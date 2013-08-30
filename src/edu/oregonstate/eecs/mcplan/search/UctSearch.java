/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.ml.GaussianMixtureModel;
import edu.oregonstate.eecs.mcplan.ml.ScoreFunctions;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Fn;


/**
 * UCT search for multiagent, general-reward domains.
 *
 * @param <S> State type
 * @param <F> State token type
 * @param <A> Action type
 */
public abstract class UctSearch<S, F extends Representer<S, F>, A extends VirtualConstructor<A>>
	implements Runnable, GameTree<Representation<S, F>, A>
{
	private static final Logger log = LoggerFactory.getLogger( UctSearch.class );
	
	public static final class Factory<S, F extends Representer<S, F>, A extends VirtualConstructor<A>>
		implements GameTreeFactory<S, F, A>
	{
		private final UndoSimulator<S, A> sim_;
		private final Representer<S, F> repr_;
		private final ActionGenerator<S, ? extends A> actions_;
		private final double c_;
		private final RandomGenerator rng_;
		private final Policy<S, A> rollout_policy_;
		private final BackupRule<Representation<S, F>, A> backup_;
		
		public Factory( final UndoSimulator<S, A> sim,
						  final Representer<S, F> repr,
						  final ActionGenerator<S, ? extends A> actions,
						  final double c, final RandomGenerator rng,
						  final Policy<S, A> rollout_policy,
						  final BackupRule<Representation<S, F>, A> backup )
		{
			sim_ = sim;
			repr_ = repr;
			actions_ = actions;
			c_ = c;
			rng_ = rng;
			rollout_policy_ = rollout_policy;
			backup_ = backup;
		}
		
		@Override
		public GameTree<Representation<S, F>, A> create( final MctsVisitor<S, A> visitor )
		{
			return new UctSearch<S, F, A>( sim_, repr_, actions_, c_, rng_, rollout_policy_, visitor )
					{
						@Override
						public double[] backup( final StateNode<Representation<S, F>, A> s )
						{
							return backup_.apply( s );
						}
					};
		}
	}
	
	public void cluster2ndLevel()
	{
		// Find all actions in a2 and assign them consecutive integer IDs
		int idx = 0;
		final Map<A, Integer> aidx = new HashMap<A, Integer>();
		for( final ActionNode<Representation<S, F>, A> a1 : Fn.in( root_.successors() ) ) {
			for( final StateNode<Representation<S, F>, A> s1 : Fn.in( a1.successors() ) ) {
				for( final ActionNode<Representation<S, F>, A> a2 : Fn.in( s1.successors() ) ) {
					if( !aidx.containsKey( a2.a ) ) {
						aidx.put( a2.a, idx++ );
					}
				}
			}
		}
		final int nactions = idx;
		
		// Encode the Q-functions of states in s1
		// FIXME: We're assuming that all states have the same player-to-act
		final ArrayList<Representation<S, F>> s1 = new ArrayList<Representation<S, F>>();
		final ArrayList<double[]> xs = new ArrayList<double[]>();
		for( final ActionNode<Representation<S, F>, A> a1 : Fn.in( root_.successors() ) ) {
			for( final StateNode<Representation<S, F>, A> s2 : Fn.in( a1.successors() ) ) {
				s1.add( s2.token );
				final Map<A, Double> q = makeQTable( s2 );
				final double[] x = new double[nactions];
				for( final Map.Entry<A, Double> qa : q.entrySet() ) {
					x[aidx.get( qa.getKey() )] = qa.getValue();
				}
				xs.add( x );
			}
		}
		System.out.println( "n = " + xs.size() );
		
		// Cluster the Q-functions
		final RandomGenerator rng = new MersenneTwister( 42 );
		double best_score = Double.MAX_VALUE;
		int best_k = 0;
		for( int k = 1; k <= Math.ceil( Math.sqrt( xs.size() ) ); ++k ) {
			System.out.println( "*** k = " + k );
			final GaussianMixtureModel gmm = new GaussianMixtureModel(
				k, xs.toArray( new double[xs.size()][] ), 10e-5, rng );
			
			// AICc is undefined when n > k + 1. It's probably bad to have
			// more than one parameter for each sample, anyway.
			if( xs.size() <= gmm.nparameters() ) {
				break;
			}
			
			gmm.run();
			for( int i = 0; i < gmm.mu().length; ++i ) {
				System.out.println( "Center " + i + ": " + gmm.mu()[i] );
			}
			
//			final double score = ScoreFunctions.aic( gmm.nparameters(), gmm.logLikelihood() );
			final double score = ScoreFunctions.aicc( xs.size(), gmm.nparameters(), gmm.logLikelihood() );
//			final double score = ScoreFunctions.bic( xs.size(), gmm.nparameters(), gmm.logLikelihood() );
			System.out.println( "Score = " + score );
			System.out.println( "ll = " + gmm.logLikelihood() );
			gmm.debug();
			if( score < best_score ) {
				best_score = score;
				best_k = k;
			}
		}
		System.out.println( "Best model: k = " + best_k );
	}
	
	/*
	public class SearchTree implements IncidenceGraph<StateNode, ActionNode>, WeightedGraph<StateNode, ActionNode>
	{
		private SearchTree( final StateNode root )
		{

		}

		@Override
		public StateNode source( final ActionNode e )
		{
			return e.source();
		}

		@Override
		public StateNode target( final ActionNode e )
		{
			return e.target();
		}

		@Override
		public Generator<A> outEdges( final StateNode v )
		{

		}

		@Override
		public int outDegree( final StateNode v )
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double weight( final ActionNode e )
		{
			// TODO Auto-generated method stub
			return 0;
		}

	}
	*/
	
	// -----------------------------------------------------------------------
	
	private final UndoSimulator<S, A> sim_;
	private final Representer<S, F> repr_;
	private final ActionGenerator<S, ? extends A> actions_;
	private final Policy<S, A> rollout_policy_;
	private final MctsVisitor<S, A> visitor_;
	private final double c_;
	private final RandomGenerator rng_;
	
	private boolean complete_ = false;
	private StateNode<Representation<S, F>, A> root_ = null;
	private Map<A, Double> qtable_ = null;
	
	public UctSearch( final UndoSimulator<S, A> sim,
					  final Representer<S, F> repr,
					  final ActionGenerator<S, ? extends A> actions,
					  final double c, final RandomGenerator rng,
					  final Policy<S, A> rollout_policy,
					  final MctsVisitor<S, A> visitor )
	{
		sim_ = sim;
		repr_ = repr;
		actions_ = actions;
		rollout_policy_ = rollout_policy;
		c_ = c;
		rng_ = rng;
		visitor_ = visitor;
	}
	
	@Override
	public void run()
	{
		final S s0 = sim_.state();
		final Representation<S, F> x0 = repr_.encode( s0 );
		root_ = new StateNode<Representation<S, F>, A>( x0, sim_.getNumAgents(), sim_.getTurn() );
		// NOTE: Making an assumption about the indices of players here.
		final int turn = sim_.getTurn();
		System.out.println( "[Uct: starting on Turn " + turn + "]" );
		visitor_.startEpisode( s0, sim_.getNumAgents(), turn );
		int rollout_count = 0;
		while( visitor_.startRollout( s0, turn ) ) {
			visit( root_, 0 /* TODO: depth limit */, visitor_ );
			rollout_count += 1;
		}
		log.info( "rollout_count = {}", rollout_count );
		qtable_ = makeQTable( root_ );
		complete_ = true;
	}
	
	private Map<A, Double> makeQTable( final StateNode<Representation<S, F>, A> root )
	{
		final Map<A, Double> q = new HashMap<A, Double>();
		for( final ActionNode<Representation<S, F>, A> an : Fn.in( root.successors() ) ) {
			q.put( an.a.create(), an.q( root.turn ) );
		}
		return q;
	}
	
	public void printTree( final PrintStream out )
	{
//		printStateNode( root_, 0, out );
		root().accept( new TreePrinter<Representation<S, F>, A>() );
	}
	
	private ActionNode<Representation<S, F>, A> selectAction(
		final StateNode<Representation<S, F>, A> sn, final ActionGenerator<S, ? extends A> actions )
	{
		assert( actions.size() > 0 );
		double max_value = -Double.MAX_VALUE;
		ActionNode<Representation<S, F>, A> max_sa = null;
		while( actions.hasNext() ) {
			final A a = actions.next();
			final ActionNode<Representation<S, F>, A> sa = sn.action( a );
			if( sa.n() == 0 ) {
				max_sa = sa;
				break;
			}
			else {
				final double exploit = sa.q( sn.turn );
				final double explore = c_ * Math.sqrt( Math.log( sn.n() ) / sa.n() );
				final double v = explore + exploit;
				if( v > max_value ) {
					max_sa = sa;
					max_value = v;
				}
			}
		}
		return max_sa;
	}
	
	private double[] rollout( final MctsVisitor<S, A> visitor )
	{
		final S s = sim_.state();
		final double[] r;
		if( sim_.isTerminalState( ) ) {
			r = visitor.terminal( s, sim_.getTurn() );
		}
		else {
			final int turn = sim_.getTurn();
			final Policy<S, A> pi = rollout_policy_;
			pi.setState( sim_.state(), sim_.t() );
			final A a = pi.getAction();
			sim_.takeAction( a );
			final S sprime = sim_.state();
			visitor.defaultAction( a, sprime, sim_.getTurn() );
			r = rollout( visitor );
			pi.actionResult( sprime, r );
			sim_.untakeLastAction();
		}
		return r;
	}
	
	private double[] visit( final StateNode<Representation<S, F>, A> sn, final int depth, final MctsVisitor<S, A> visitor )
	{
		final S s = sim_.state();
		sn.visit();
		if( sim_.isTerminalState( ) ) {
			return visitor.terminal( s, sim_.getTurn() );
		}
		else {
			final int turn = sim_.getTurn();
//			assert( sn.turn == turn );
			final ActionGenerator<S, ? extends A> action_gen = actions_.create();
			action_gen.setState( s, sim_.t(), turn );
			final ActionNode<Representation<S, F>, A> sa = selectAction( sn, action_gen );
			sa.visit();
			sim_.takeAction( sa.a );
			final double[] r = sim_.getReward();
			final S sprime = sim_.state();
			visitor.treeAction( sa.a, sprime, sim_.getTurn() );
			final double[] q;
			if( sa.n() == 1 ) {
				// Leaf node
				q = rollout( visitor );
			}
			else {
				final Representation<S, F> x = repr_.encode( sprime );
				final StateNode<Representation<S, F>, A> snprime = sa.stateNode( x, sim_.getTurn() );
				q = visit( snprime, depth - 1, visitor );
			}
			sa.updateR( r );
			sa.updateQ( q );
			sim_.untakeLastAction();
			return backup( sn );
		}
	}

	public Map<A, Double> qtable()
	{
		return qtable_;
	}

	public boolean isComplete()
	{
		return complete_;
	}

	@Override
	public StateNode<Representation<S, F>, A> root()
	{
		return root_;
	}
}
