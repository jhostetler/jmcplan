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


/**
 * Adversarial UCT search for two-player, zero-sum games. Implemented in the
 * "negamax" style.
 *
 * @param <S> State type
 * @param <T> State token type
 * @param <A> Action type
 */
public class UctSearch<S, F extends Representer<S, F>, A extends VirtualConstructor<A>>
	implements Runnable, GameTree<UctStateNode<S, F, A>, UctActionNode<S, F, A>>
{
	private static final Logger log = LoggerFactory.getLogger( UctSearch.class );
	
	/*
	private class ActionNode implements GameTreeNode<StateNode, ActionNode>
	{
		public final A a;
		private int n_ = 0;
		private final MeanVarianceAccumulator mv_ = new MeanVarianceAccumulator();
		public final Map<Representation<S, F>, StateNode> m_ = new HashMap<Representation<S, F>, StateNode>();

		public ActionNode( final A a )
		{
			this.a = a;
		}

		public StateNode stateNode( final Representation<S, F> token, final int turn )
		{
			StateNode si = m_.get( token );
			if( si == null ) {
//				System.out.println( "miss " + toString() + " " + hashCode() + " " + token.hashCode() );
				si = new StateNode( token, turn );
				m_.put( token, si );
			}
//			else {
//				System.out.println( "hit " + toString() + " " + hashCode() + " " + token.hashCode() );
//			}
			return si;
		}

		public Generator<StateNode> successors()
		{
			return Generator.fromIterator( m_.values().iterator() );
		}

		public int n()
		{ return n_; }

		public void visit()
		{ n_ += 1; }

		public void updateQ( final double q )
		{ mv_.add( q ); }

		public double q()
		{ return mv_.mean(); }

		public double qvar()
		{ return mv_.variance(); }

		@Override
		public String toString()
		{ return a.toString(); }

		@Override
		public void visit( final GameTreeVisitor<StateNode, ActionNode> visitor )
		{
			visitor.actionNode( this );
		}
	}

	private class StateNode implements GameTreeNode<StateNode, ActionNode>
	{
		public final Map<A, ActionNode> a_ = new HashMap<A, ActionNode>();

		private int n_ = 0;
		public final int turn;
		public final Representation<S, F> token;

		public StateNode( final Representation<S, F> token, final int turn )
		{
			this.token = token;
			this.turn = turn;
		}

		public int n()
		{ return n_; }

		public void visit()
		{ n_ += 1; }

		public ActionNode action( final A a )
		{
			ActionNode sa = a_.get( a );
			if( sa == null ) {
				sa = new ActionNode( a );
				a_.put( a, sa );
			}
			return sa;
		}

		public ActionNode bestAction( final RandomGenerator rng )
		{
			// Find all actions with maximal Q-value, and choose one at
			// random. Randomness is necessary because in the late game, if
			// the rollout policy always wins, all actions look the same, but
			// the UCT policy might always pick one that never actually wins
			// the game due to fixed action generator ordering.
			double max_q = -Double.MAX_VALUE;
			final ArrayList<ActionNode> action_pool = new ArrayList<ActionNode>();
			for( final Map.Entry<A, ActionNode> e : a_.entrySet() ) {
				log.info( "Action {}: n = {}, q = {}, 95% = {}",
						  e.getKey(), e.getValue().n(), e.getValue().q(), 2 * Math.sqrt( e.getValue().qvar() ) );
				final double q = e.getValue().q();
				if( q > max_q ) {
					max_q = q;
					action_pool.clear();
				}
				if( q == max_q ) {
					action_pool.add( e.getValue() );
				}
			}
			log.info( "Action pool: {}", action_pool );
			final int random_action = rng_.nextInt( action_pool.size() );
			System.out.println( "--> Selected " + action_pool.get( random_action ) );
			return action_pool.get( random_action );
		}

		@Override
		public void visit( final GameTreeVisitor<StateNode, ActionNode> visitor )
		{
			visitor.stateNode( this );
		}
	}
	*/
	
	public void cluster2ndLevel()
	{
		// Find all actions in a2 and assign them consecutive integer IDs
		int idx = 0;
		final Map<A, Integer> aidx = new HashMap<A, Integer>();
		for( final Map.Entry<A, UctActionNode<S, F, A>> ae1 : root_.a_.entrySet() ) {
			for( final Map.Entry<Representation<S, F>, UctStateNode<S, F, A>> se1 : ae1.getValue().m_.entrySet() ) {
				for( final Map.Entry<A, UctActionNode<S, F, A>> ae2 : se1.getValue().a_.entrySet() ) {
					if( !aidx.containsKey( ae2.getKey() ) ) {
						aidx.put( ae2.getKey(), idx++ );
					}
				}
			}
		}
		final int nactions = idx;
		
		// Encode the Q-functions of states in s1
		final ArrayList<Representation<S, F>> s1 = new ArrayList<Representation<S, F>>();
		final ArrayList<double[]> xs = new ArrayList<double[]>();
		for( final Map.Entry<A, UctActionNode<S, F, A>> ae1 : root_.a_.entrySet() ) {
			for( final Map.Entry<Representation<S, F>, UctStateNode<S, F, A>> se1 : ae1.getValue().m_.entrySet() ) {
				s1.add( se1.getKey() );
				final Map<A, Double> q = makeQTable( se1.getValue() );
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
	private final ArrayList<Policy<S, A>> rollout_policies_;
	private final MctsVisitor<S, A> visitor_;
	private final double c_;
	private final RandomGenerator rng_;
	
	private boolean complete_ = false;
	private UctStateNode<S, F, A> root_ = null;
	private Map<A, Double> qtable_ = null;
	
	public UctSearch( final UndoSimulator<S, A> sim,
					  final Representer<S, F> repr,
					  final ActionGenerator<S, ? extends A> actions,
					  final double c, final RandomGenerator rng,
					  final ArrayList<Policy<S, A>> rollout_policies,
					  final MctsVisitor<S, A> visitor )
	{
		sim_ = sim;
		repr_ = repr;
		actions_ = actions;
		rollout_policies_ = rollout_policies;
		c_ = c;
		rng_ = rng;
		visitor_ = visitor;
	}
	
	@Override
	public void run()
	{
		final S s0 = sim_.state();
		final Representation<S, F> x0 = repr_.encode( s0 );
		root_ = new UctStateNode<S, F, A>( x0, sim_.getTurn() );
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
	
	private Map<A, Double> makeQTable( final UctStateNode<S, F, A> root )
	{
		final Map<A, Double> q = new HashMap<A, Double>();
		for( final Map.Entry<A, UctActionNode<S, F, A>> e : root.a_.entrySet() ) {
			q.put( e.getKey().create(), e.getValue().q() );
		}
		return q;
	}
	
	public class TreePrinter implements GameTreeVisitor<UctStateNode<S, F, A>, UctActionNode<S, F, A>>
	{
		private final PrintStream out_ = System.out;
		private int d_ = 0;
		
		@Override
		public void stateNode( final UctStateNode<S, F, A> s )
		{
			for( int i = 0; i < d_; ++i ) {
				out_.print( "  " );
			}
			out_.print( "S" );
			out_.print( d_ / 2 );
			out_.print( ": n = " );
			out_.print( s.n() );
			out_.print( ": " );
			out_.println( s.token );
			d_ += 1;
			for( final Map.Entry<A, UctActionNode<S, F, A>> e : s.a_.entrySet() ) {
				e.getValue().visit( this );
			}
			d_ -= 1;
		}

		@Override
		public void actionNode( final UctActionNode<S, F, A> a )
		{
			for( int i = 0; i < d_; ++i ) {
				out_.print( "  " );
			}
			out_.print( "A" );
			out_.print( d_ / 2 );
			out_.print( ": n = " );
			out_.print( a.n() );
			out_.print( ": " );
			out_.print( a.a );
			out_.print( ": q = " );
			out_.print( a.q() );
			out_.print( ", var = " );
			out_.print( a.qvar() );
			out_.print( ", 95% = " );
			out_.print( 2 * Math.sqrt( a.qvar() ) );
			out_.println();
			d_ += 1;
			for( final Map.Entry<Representation<S, F>, UctStateNode<S, F, A>> e : a.m_.entrySet() ) {
				e.getValue().visit( this );
			}
			d_ -= 1;
		}
		
	}
	
	public void printTree( final PrintStream out )
	{
//		printStateNode( root_, 0, out );
		root().visit( new TreePrinter() );
	}
	
	private void printStateNode( final UctStateNode<S, F, A> sn, final int depth, final PrintStream out )
	{
		for( int i = 0; i < depth; ++i ) {
			out.print( "  " );
		}
		out.print( "S" );
		out.print( depth / 2 );
		out.print( ": n = " );
		out.print( sn.n() );
		out.print( ": " );
		out.println( sn.token );
		for( final Map.Entry<A, UctActionNode<S, F, A>> e : sn.a_.entrySet() ) {
			printActionNode( e.getValue(), depth + 1, out );
		}
	}
	
	private void printActionNode( final UctActionNode<S, F, A> an, final int depth, final PrintStream out )
	{
		for( int i = 0; i < depth; ++i ) {
			out.print( "  " );
		}
		out.print( "A" );
		out.print( depth / 2 );
		out.print( ": n = " );
		out.print( an.n() );
		out.print( ": " );
		out.print( an.a );
		out.print( ": q = " );
		out.print( an.q() );
		out.print( ", var = " );
		out.print( an.qvar() );
		out.print( ", 95% = " );
		out.print( 2 * Math.sqrt( an.qvar() ) );
		out.println();
		for( final Map.Entry<Representation<S, F>, UctStateNode<S, F, A>> e : an.m_.entrySet() ) {
			printStateNode( e.getValue(), depth + 1, out );
		}
	}
	
	private UctActionNode<S, F, A> selectAction( final UctStateNode<S, F, A> sn, final ActionGenerator<S, ? extends A> actions )
	{
		assert( actions.size() > 0 );
		double max_value = -Double.MAX_VALUE;
		UctActionNode<S, F, A> max_sa = null;
		while( actions.hasNext() ) {
			final A a = actions.next();
			final UctActionNode<S, F, A> sa = sn.action( a );
			if( sa.n() == 0 ) {
				max_sa = sa;
				break;
			}
			else {
				final double exploit = sa.q();
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
		if( sim_.isTerminalState( s ) ) {
			r = visitor.terminal( s, sim_.getTurn() );
		}
		else {
			final int turn = sim_.getTurn();
			final Policy<S, A> pi = rollout_policies_.get( turn );
			pi.setState( sim_.state(), sim_.t() );
			final A a = pi.getAction();
			sim_.takeAction( a );
			final S sprime = sim_.state();
			visitor.defaultAction( a, sprime, sim_.getTurn() );
			r = rollout( visitor );
			pi.actionResult( a, sprime, r[turn] );
			sim_.untakeLastAction();
		}
		return r;
	}
	
	private double[] visit( final UctStateNode<S, F, A> sn, final int depth, final MctsVisitor<S, A> visitor )
	{
		final S s = sim_.state();
		sn.visit();
		final double[] r;
		if( sim_.isTerminalState( s ) ) {
			r = visitor.terminal( s, sim_.getTurn() );
		}
		else {
			final int turn = sim_.getTurn();
			assert( sn.turn == turn );
			final ActionGenerator<S, ? extends A> action_gen = actions_.create();
			action_gen.setState( s, sim_.t(), turn );
			final UctActionNode<S, F, A> sa = selectAction( sn, action_gen );
			sa.visit();
			sim_.takeAction( sa.a );
			final S sprime = sim_.state();
			visitor.treeAction( sa.a, sprime, sim_.getTurn() );
			if( sa.n() == 1 ) {
				// Leaf node
				r = rollout( visitor );
			}
			else {
				final Representation<S, F> x = repr_.encode( sprime );
				final UctStateNode<S, F, A> snprime = sa.stateNode( x, sim_.getTurn() );
				r = visit( snprime, depth - 1, visitor );
			}
			sa.updateQ( r[turn] );
			sim_.untakeLastAction();
		}
		return r;
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
	public GameTreeNode<UctStateNode<S, F, A>, UctActionNode<S, F, A>> root()
	{
		return root_;
	}
}
