/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.random.MersenneTwister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.ActionGen;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.IdentityRepresenter;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.Simulator;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.State;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.Visitor;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Countdown;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public abstract class SparseSampleTree<S, F extends Representer<S, F>, A extends VirtualConstructor<A>>
	implements Runnable, GameTree<Representation<S, F>, A>
{
	private static final Logger log = LoggerFactory.getLogger( SparseSampleTree.class );
	
	private final UndoSimulator<S, A> sim_;
	private final Representer<S, F> repr_;
	private final ActionGenerator<S, ? extends A> actions_;
	private final MctsVisitor<S, A> visitor_;
	private final int width_;
	private final int depth_;
	
	private boolean complete_ = false;
	private StateNode<Representation<S, F>, A> root_ = null;
	private Map<A, Double> qtable_ = null;
	
	public SparseSampleTree( final UndoSimulator<S, A> sim,
					  final Representer<S, F> repr,
					  final ActionGenerator<S, ? extends A> actions,
					  final int width, final int depth,
					  final MctsVisitor<S, A> visitor )
	{
		sim_ = sim;
		repr_ = repr;
		actions_ = actions;
		width_ = width;
		depth_ = depth;
		visitor_ = visitor;
	}

	@Override
	public StateNode<Representation<S, F>, A> root()
	{
		return root_;
	}

	@Override
	public void run()
	{
		final S s0 = sim_.state();
		final Representation<S, F> x0 = repr_.encode( s0 );
		root_ = new StateNode<Representation<S, F>, A>( x0, sim_.getNumAgents(), sim_.getTurn() );
		// NOTE: Making an assumption about the indices of players here.
		final int turn = sim_.getTurn();
		System.out.println( "[SS: starting on Turn " + turn + "]" );
		visitor_.startEpisode( s0, sim_.getNumAgents(), turn );
//		int rollout_count = 0;
//		while( visitor_.startRollout( s0, turn ) ) {
//			visit( root_, depth_, visitor_ );
//			rollout_count += 1;
//		}
//		log.info( "rollout_count = {}", rollout_count );
		visit( root_, depth_, visitor_ );
		qtable_ = makeQTable( root_ );
		complete_ = true;
	}
	
	private double[] visit( final StateNode<Representation<S, F>, A> sn, final int depth, final MctsVisitor<S, A> visitor )
	{
		final S s = sim_.state();
		sn.visit();
		if( sim_.isTerminalState() ) {
			// Note: This is not a backed-up value
			return visitor.terminal( s, sim_.getTurn() );
		}
		else if( depth == 0 ) {
			return new double[sim_.getNumAgents()];
		}
		else {
			final int turn = sim_.getTurn();
//			assert( sn.turn == turn );
			final ActionGenerator<S, ? extends A> action_gen = actions_.create();
			action_gen.setState( s, sim_.t(), turn );
			
			while( action_gen.hasNext() ) {
				final A a = action_gen.next();
				final ActionNode<Representation<S, F>, A> sa = sn.action( a );
				for( int w = 0; w < width_; ++w ) {
					sa.visit();
					sim_.takeAction( sa.a );
					final double[] r = sim_.getReward();
					final S sprime = sim_.state();
					visitor.treeAction( sa.a, sprime, sim_.getTurn() );
					final Representation<S, F> x = repr_.encode( sprime );
					final StateNode<Representation<S, F>, A> snprime = sa.stateNode( x, sim_.getTurn() );
					final double[] qi = visit( snprime, depth - 1, visitor );
					sa.updateQ( qi );
					sa.updateR( r );
					sim_.untakeLastAction();
				}
			}
			return backup( sn );
		}
	}
	
	@Override
	public abstract double[] backup( final StateNode<Representation<S, F>, A> s );
	
	private Map<A, Double> makeQTable( final StateNode<Representation<S, F>, A> root )
	{
		final Map<A, Double> q = new HashMap<A, Double>();
		for( final ActionNode<Representation<S, F>, A> an : Fn.in( root.successors() ) ) {
			q.put( an.a.create(), an.q( root.turn ) );
		}
		return q;
	}
	
	public static void main( final String[] args )
	{
		final MersenneTwister rng = new MersenneTwister( 42 );
		final Simulator sim = new Simulator();
		final int width = 40;
		final int depth = 4;
		
		final SparseSampleTree<State, IdentityRepresenter, UndoableAction<State>> tree
			= new SparseSampleTree<State, IdentityRepresenter, UndoableAction<State>>(
				sim, new IdentityRepresenter(), new ActionGen( rng ), width, depth,
				TimeLimitMctsVisitor.create( new Visitor<UndoableAction<State>>(), new Countdown( 1000 ) ) )
			{
				@Override
				public double[] backup( final StateNode<Representation<State, IdentityRepresenter>, UndoableAction<State>> s )
				{
					double max_q = -Double.MAX_VALUE;
					for( final ActionNode<Representation<State, IdentityRepresenter>, UndoableAction<State>> an : Fn.in( s.successors() ) ) {
						if( an.q( 0 ) > max_q ) {
							max_q = an.q( 0 );
						}
					}
					return new double[] { max_q };
				}
			};
		tree.run();
		tree.root().accept( new TreePrinter<Representation<State, IdentityRepresenter>, UndoableAction<State>>() );
	}

}
