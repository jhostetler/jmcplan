/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.Tokenizable;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 * @param <T>
 *
 */
public class UctSearcher<S extends Tokenizable<T>, T, A extends UndoableAction<S>>
	implements AnytimePolicy<S, A>
{
	private static final Logger log = LoggerFactory.getLogger( UctSearcher.class );
	
	private class ActionNode
	{
		public final A a;
		public int n = 0;
		private final MeanVarianceAccumulator mv_ = new MeanVarianceAccumulator();
		private final Map<T, StateNode> m_ = new HashMap<T, StateNode>();
		
		public ActionNode( final A a )
		{
			this.a = a;
		}
		
		public StateNode stateNode( final T token )
		{
			StateNode si = m_.get( token );
			if( si == null ) {
				si = new StateNode( token );
				m_.put( token, si );
			}
			return si;
		}
		
		public void updateQ( final double q )
		{ mv_.add( q ); }
		
		public double q()
		{ return mv_.mean(); }
		
		public double qvar()
		{ return mv_.variance(); }
	}
	
	private class StateNode
	{
		private final Map<UndoableAction<S>, ActionNode> a_ = new HashMap<UndoableAction<S>, ActionNode>();
		
		public int n = 0;
		public final T token;
		
		public StateNode( final T token )
		{
			this.token = token;
		}
		
		public ActionNode action( final A a )
		{
			ActionNode sa = a_.get( a );
			if( sa == null ) {
				sa = new ActionNode( a );
				a_.put( a, sa );
			}
			return sa;
		}
		
		public ActionNode bestAction()
		{
			final int max_n = 0;
			double max_q = -Double.MAX_VALUE;
			ActionNode best_action = null;
			for( final Map.Entry<UndoableAction<S>, ActionNode> e : a_.entrySet() ) {
				log.info( "Action {}: n = {}, q = {}, 95% = {}",
						  e.getKey(), e.getValue().n, e.getValue().q(), 2 * Math.sqrt( e.getValue().qvar() ) );
				if( e.getValue().q() > max_q ) {
					max_q = e.getValue().q();
					best_action = e.getValue();
				}
			}
			return best_action;
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final UndoSimulator<S, A> sim_;
	private final ActionGenerator<S, ? extends A> actions_;
	private final ArrayList<Policy<S, A>> rollout_policies_;
	private final MctsNegamaxVisitor<S, A> visitor_;
	private final double c_;
	
	private boolean complete_ = false;
	private StateNode root_ = null;
	private PrincipalVariation<T, UndoableAction<S>> pv_ = null;
	
	public UctSearcher( final UndoSimulator<S, A> sim,
					  final ActionGenerator<S, ? extends A> actions, final double c,
					  final ArrayList<Policy<S, A>> rollout_policies,
					  final MctsNegamaxVisitor<S> visitor )
	{
		sim_ = sim;
		actions_ = actions;
		rollout_policies_ = rollout_policies;
		c_ = c;
		visitor_ = visitor;
	}
	
	@Override
	public void run()
	{
		final S s0 = sim_.state();
		root_ = new StateNode( s0.token() );
		// NOTE: Making an assumption about the indices of players here.
		final int turn = sim_.getTurn();
		System.out.println( "[Uct: starting on Turn " + turn + "]" );
		visitor_.startEpisode( s0 );
		int rollout_count = 0;
		while( visitor_.startRollout( s0 ) ) {
			visit( root_, 0 /* TODO: depth limit */, 1, visitor_ );
			rollout_count += 1;
		}
		log.info( "rollout_count = {}", rollout_count );
		pv_ = new PrincipalVariation<T, UndoableAction<S>>( 1 );
		final ActionNode astar = root_.bestAction();
		pv_.states.set( 0, s0.token() );
		pv_.actions.set( 0, astar.a.create() );
		pv_.score = astar.q();
		complete_ = true;
	}
	
	private ActionNode selectAction( final StateNode sn, final ActionGenerator<S, ? extends A> actions )
	{
		assert( actions.size() > 0 );
		double max_value = -Double.MAX_VALUE;
		ActionNode max_sa = null;
		while( actions.hasNext() ) {
			final A a = actions.next();
			final ActionNode sa = sn.action( a );
			if( sa.n == 0 ) {
				max_sa = sa;
				break;
			}
			else {
				final double exploit = sa.q();
				final double explore = c_ * Math.sqrt( Math.log( sn.n ) / sa.n );
				final double v = explore + exploit;
				if( v > max_value ) {
					max_sa = sa;
					max_value = v;
				}
			}
		}
		return max_sa;
	}
	
	private double rollout( final int color, final MctsNegamaxVisitor<S> visitor )
	{
		final S s = sim_.state();
		if( sim_.isTerminalState( ) ) {
			return color * visitor.terminal( s );
		}
		
		final Policy<S, A> pi = rollout_policies_.get( sim_.getTurn() );
		pi.setState( sim_.state(), sim_.t() );
		final A a = pi.getAction();
		sim_.takeAction( a );
		final S sprime = sim_.state();
		visitor.defaultAction( a, sprime );
		final double r = color * rollout( -color, visitor );
		pi.actionResult( sprime, r );
		sim_.untakeLastAction();
		return r;
	}
	
	private double visit( final StateNode sn, final int depth, final int color, final MctsNegamaxVisitor<S> visitor )
	{
		assert( color * sim_.getTurn() <= 0 );
		final S s = sim_.state();
		sn.n += 1;
		if( sim_.isTerminalState( ) ) {
			return color * visitor.terminal( s );
		}
		final int turn = sim_.getTurn();
		final ActionGenerator<S, ? extends A> action_gen = actions_.create();
		action_gen.setState( s, sim_.t(), turn );
		final ActionNode sa = selectAction( sn, action_gen );
		sa.n += 1;
		sim_.takeAction( sa.a );
		final S sprime = sim_.state();
		visitor.treeAction( sa.a, sprime );
		final StateNode snprime = sa.stateNode( sprime.token() );
		final double r;
		if( sa.n == 1 ) {
			// Leaf node
			r = color * rollout( -color, visitor );
		}
		else {
			r = color * visit( snprime, depth - 1, -color, visitor );
		}
		sa.updateQ( r );
		sim_.untakeLastAction();
		return r;
	}

	@Override
	public double score()
	{
		return pv_.score;
	}

	@Override
	public PrincipalVariation<T, UndoableAction<S>> principalVariation()
	{
		return pv_;
	}

	@Override
	public boolean isComplete()
	{
		return complete_;
	}

	@Override
	public void setState( final S s, final long t )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public A getAction()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long minControl()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long maxControl()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public A getAction( final long control )
	{
		// TODO Auto-generated method stub
		return null;
	}
}
