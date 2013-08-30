/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.search.MctsVisitor;
import edu.oregonstate.eecs.mcplan.search.UctSearcher;
import edu.oregonstate.eecs.mcplan.sim.OptionListener;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;
import gnu.trove.stack.TDoubleStack;
import gnu.trove.stack.array.TDoubleArrayStack;

/**
 * @author jhostetler
 *
 */
public class Controller<S extends State<S, T>, T, A>
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
		private final Map<A, ActionNode> a_ = new HashMap<A, ActionNode>();
		
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
			for( final Map.Entry<A, ActionNode> e : a_.entrySet() ) {
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
	
	public class SarTuple
	{
		public final T s;
		public final A a;
		public final double r;
		
		public SarTuple( final T s, final A a, final double r )
		{
			this.s = s;
			this.a = a;
			this.r = r;
		}
	}
	
	private final UndoSimulator<S, A> sim_;
	private final ActionGenerator<S, Option<S, A>> actions_;
	private final ArrayList<Policy<S, Option<S, A>>> rollout_policies_;
	private final int T_;
	private final double c_;
	private final MctsVisitor<S, A> visitor_;
	
	private final ArrayList<Option<S, A>> active_;
	// TODO: seed
	private final RandomGenerator rng_ = new MersenneTwister();
	int color = 1;
	private S s_ = null;
	private final TDoubleStack rhist_ = new TDoubleArrayStack();
	
	private StateNode root_ = null;
	private StateNode sn = null;
	private ActionNode sa = null;
	private StateNode snprime = null;
	private int episode_count = 0;
	private final int turn_ = 0;
	private ActionGenerator<S, ? extends A> action_gen_ = null;
	private boolean in_rollout_ = false;
	private Option<S, A> active_option_ = null;
	
	public Controller( final UndoSimulator<S, A> sim,
					   final ActionGenerator<S, Option<S, A>> actions,
					   final ArrayList<Policy<S, Option<S, A>>> rollout_policies,
					   final int T, final double c,
					   final MctsVisitor<S, A> visitor )
	{
		sim_ = sim;
		actions_ = actions;
		rollout_policies_ = rollout_policies;
		assert( sim.getNumAgents() == 2 );
		assert( rollout_policies.size() == 2 );
		T_ = T;
		c_ = c;
		visitor_ = visitor;
		active_ = new ArrayList<Option<S, A>>( sim_.getNumAgents() );
		Collections.fill( active_, null );
	}
	
	public boolean terminate( final S s, final Option<S, A> o )
	{
		final double beta = o.terminate( s );
		if( beta == 1.0 ) {
			return true;
		}
		else if( beta == 0.0 ) {
			return false;
		}
		else {
			return rng_.nextDouble() < beta;
		}
	}
	
	public void run()
	{
		final S s0 = sim_.state();
		for( int i = 0; i < policies_.size(); ++i ) {
			final Policy<S, Option<S, A>> pi = policies_.get( i );
			pi.setState( s0, 0L );
			final Option<S, A> o = pi.getAction();
			active_.set( i, o );
			o.start( s0 );
			fireOptionInitiated( s0, i, o );
		}
		fireStartState( s0 );
		for( int t = 0; t < T_; ++t ) {
			final ArrayList<A> actions = new ArrayList<A>( policies_.size() );
			for( int i = 0; i < policies_.size(); ++i ) {
				final S s = sim_.state();
				Option<S, A> option = active_.get( i );
				if( terminate( s, option ) ) {
					// TODO: How to split 'control' up between option choice
					// and action choice?
					fireOptionTerminated( s, i, option );
					option = policies_.get( i ).getAction();
					option.start( s );
					fireOptionInitiated( s, i, option );
				}
				final Policy<S, A> policy = option.pi;
				System.out.println( "[SimultaneousMoveRunner] Action selection: setTurn( " + i + " )" );
				sim_.setTurn( i );
				policy.setState( s, t );
				firePreGetAction( i );
				final A a = policy.getAction();
				firePostGetAction( i, a );
				actions.add( a );
				System.out.println( "!!! [t = " + t + "] a" + i + " = " + a.toString() );
			}
			System.out.println( "[SimultaneousMoveRunner] Execution: setTurn( 0 )" );
			sim_.setTurn( 0 );
			for( final A a : actions ) {
				sim_.takeAction( a );
			}
			fireActionsTaken( sim_.state() );
			if( sim_.isTerminalState( ) ) {
				break;
			}
		}
		fireEndState( sim_.state() );
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
	
	public void startState( final S s0 )
	{
		root_ = new StateNode( s0.token() );
	}
	
	public void startEpisode()
	{
		sn = root_;
		color = 1;
		episode_count += 1;
		in_rollout_ = false;
	}
	
	/**
	 * Return true to terminate episode.
	 * @param s
	 * @param player
	 * @param t
	 * @return
	 */
	public boolean setState( final S s, final int player, final long t )
	{
		s_ = sim_.state();
		turn_ = player;
		if( sim_.isTerminalState( ) ) {
			return true;
		}
		
		Option<S, A> option = active_.get( player );
		if( terminate( s, option ) ) {
			// TODO: How to split 'control' up between option choice
			// and action choice?
//			fireOptionTerminated( s, player, option );
			option = policies_.get( player ).getAction();
			option.start( s );
//			fireOptionInitiated( s, player, option );
		}
		
		if( in_rollout_ ) {
			active_option_ = rollout_policies_.get( turn_ );
		}
		else {
			action_gen_ = actions_.create();
			action_gen_.setState( s, t, player );
		}
		
		
	}
	
	public A getAction()
	{
		assert( color * sim_.getTurn() <= 0 );
		if( in_rollout_ ) {
			final Policy<S, A>
		}
		
		sn.n += 1;
		
		sa = selectAction( sn, action_gen_ );
		sa.n += 1;
		
		if( sa.n == 1 ) {
			// Leaf node
			in_rollout_ = true;
		}
		
		return sa.a;
	}
	
	/**
	 * This function may be called by the execution environment to provide
	 * reward feedback. The default implementation is a no-op.
	 * @param s
	 * @param a
	 * @param sprime
	 * @param r
	 */
	public void actionResult( final int player, final A a, final S sprime, final double r )
	{
		// FIXME: Not handling turn properly yet
		rhist_.push( r );
		visitor_.treeAction( sa.a, sprime );
		snprime = sa.stateNode( sprime.token() );
		sa.updateQ( r );
	}
	
	public String getName();
	
	@Override
	public int hashCode();
	
	@Override
	public boolean equals( final Object that );
	
	// -----------------------------------------------------------------------
	
	public void addListener( final OptionListener<S, A> listener )
	{
		listeners_.add( listener );
	}
	
	private void fireStartState( final S s )
	{
		final ArrayList<Policy<S, A>> Pi = new ArrayList<Policy<S, A>>();
		for( final Option<S, A> o : active_ ) {
			Pi.add( o.pi );
		}
		for( final OptionListener<S, A> listener : listeners_ ) {
			listener.startState( s, Pi );
		}
	}
	
	private void firePreGetAction( final int i )
	{
		for( final OptionListener<S, A> listener : listeners_ ) {
			listener.preGetAction( i );
		}
	}
	
	private void fireOptionTerminated( final S s, final int i, final Option<S, A> o )
	{
		for( final OptionListener<S, A> listener : listeners_ ) {
			listener.optionTerminated( s, i, o );
		}
	}
	
	private void fireOptionInitiated( final S s, final int i, final Option<S, A> o )
	{
		for( final OptionListener<S, A> listener : listeners_ ) {
			listener.optionInitiated( s, i, o );
		}
	}
	
	private void firePostGetAction( final int i, final UndoableAction<S> a )
	{
		for( final OptionListener<S, A> listener : listeners_ ) {
			listener.postGetAction( i, a );
		}
	}
	
	private void fireActionsTaken( final S sprime )
	{
		for( final OptionListener<S, A> listener : listeners_ ) {
			listener.onActionsTaken( sprime );
		}
	}
	
	private void fireEndState( final S s )
	{
		for( final OptionListener<S, A> listener : listeners_ ) {
			listener.endState( s );
		}
	}
}
