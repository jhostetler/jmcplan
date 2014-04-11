/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.io.PrintStream;

import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Fn;


/**
 * UCT search for multiagent, general-reward domains.
 *
 * @param <S> State type
 * @param <F> State token type
 * @param <A> Action type
 */
public abstract class UctSearch<S extends State, X extends Representation<S>, A extends VirtualConstructor<A>>
	implements Runnable, GameTree<X, A>
{
	private static final Logger log = LoggerFactory.getLogger( UctSearch.class );
	
	public static final class Factory<S extends State, X extends Representation<S>, A extends VirtualConstructor<A>>
		implements GameTreeFactory<S, X, A>
	{
		private final UndoSimulator<S, A> sim_;
		private final Representer<S, X> repr_;
		private final ActionGenerator<S, JointAction<A>> actions_;
		private final double c_;
		private final int episode_limit_;
		private final RandomGenerator rng_;
		private final EvaluationFunction<S, A> eval_;
		private final BackupRule<X, A> backup_;
		private final double[] default_value_;
		
		public Factory( final UndoSimulator<S, A> sim,
						  final Representer<S, X> repr,
						  final ActionGenerator<S, JointAction<A>> actions,
						  final double c, final int episode_limit,
						  final RandomGenerator rng,
						  final EvaluationFunction<S, A> eval,
						  final BackupRule<X, A> backup, final double[] default_value )
		{
			sim_ = sim;
			repr_ = repr;
			actions_ = actions;
			c_ = c;
			episode_limit_ = episode_limit;
			rng_ = rng;
			eval_ = eval;
			backup_ = backup;
			default_value_ = default_value;
		}
		
		@Override
		public GameTree<X, A> create( final MctsVisitor<S, X, A> visitor )
		{
			return new UctSearch<S, X, A>( sim_, repr_, actions_, c_, episode_limit_,
										   rng_, eval_, visitor )
			{
				@Override
				protected MutableStateNode<S, X, A> createStateNode(
					final MutableActionNode<S, X, A> an, final X x, final int nagents, final int[] turn )
				{
					return new DelegateStateNode<S, X, A>( backup_, default_value_, x, nagents, turn );
				}
				
				@Override
				protected MutableStateNode<S, X, A> fetchStateNode(
					final MutableActionNode<S, X, A> an, final X x, final int nagents, final int[] turn )
				{
					for( final MutableStateNode<S, X, A> agg : Fn.in( an.successors() ) ) {
						if( agg.token.equals( x ) ) {
							return agg;
						}
					}
					return null;
				}
			};
		}
	}
	
//	public void cluster2ndLevel()
//	{
//		// Find all actions in a2 and assign them consecutive integer IDs
//		int idx = 0;
//		final Map<A, Integer> aidx = new HashMap<A, Integer>();
//		for( final ActionNode<Representation<S, F>, A> a1 : Fn.in( root_.successors() ) ) {
//			for( final StateNode<Representation<S, F>, A> s1 : Fn.in( a1.successors() ) ) {
//				for( final ActionNode<Representation<S, F>, A> a2 : Fn.in( s1.successors() ) ) {
//					if( !aidx.containsKey( a2.a ) ) {
//						aidx.put( a2.a, idx++ );
//					}
//				}
//			}
//		}
//		final int nactions = idx;
//
//		// Encode the Q-functions of states in s1
//		// FIXME: We're assuming that all states have the same player-to-act
//		final ArrayList<Representation<S, F>> s1 = new ArrayList<Representation<S, F>>();
//		final ArrayList<double[]> xs = new ArrayList<double[]>();
//		for( final ActionNode<Representation<S, F>, A> a1 : Fn.in( root_.successors() ) ) {
//			for( final StateNode<Representation<S, F>, A> s2 : Fn.in( a1.successors() ) ) {
//				s1.add( s2.token );
//				final Map<A, Double> q = makeQTable( s2 );
//				final double[] x = new double[nactions];
//				for( final Map.Entry<A, Double> qa : q.entrySet() ) {
//					x[aidx.get( qa.getKey() )] = qa.getValue();
//				}
//				xs.add( x );
//			}
//		}
//		System.out.println( "n = " + xs.size() );
//
//		// Cluster the Q-functions
//		final RandomGenerator rng = new MersenneTwister( 42 );
//		double best_score = Double.MAX_VALUE;
//		int best_k = 0;
//		for( int k = 1; k <= Math.ceil( Math.sqrt( xs.size() ) ); ++k ) {
//			System.out.println( "*** k = " + k );
//			final GaussianMixtureModel gmm = new GaussianMixtureModel(
//				k, xs.toArray( new double[xs.size()][] ), 10e-5, rng );
//
//			// AICc is undefined when n > k + 1. It's probably bad to have
//			// more than one parameter for each sample, anyway.
//			if( xs.size() <= gmm.nparameters() ) {
//				break;
//			}
//
//			gmm.run();
//			for( int i = 0; i < gmm.mu().length; ++i ) {
//				System.out.println( "Center " + i + ": " + gmm.mu()[i] );
//			}
//
////			final double score = ScoreFunctions.aic( gmm.nparameters(), gmm.logLikelihood() );
//			final double score = ScoreFunctions.aicc( xs.size(), gmm.nparameters(), gmm.logLikelihood() );
////			final double score = ScoreFunctions.bic( xs.size(), gmm.nparameters(), gmm.logLikelihood() );
//			System.out.println( "Score = " + score );
//			System.out.println( "ll = " + gmm.logLikelihood() );
//			gmm.debug();
//			if( score < best_score ) {
//				best_score = score;
//				best_k = k;
//			}
//		}
//		System.out.println( "Best model: k = " + best_k );
//	}
	
	// -----------------------------------------------------------------------
	
	private final UndoSimulator<S, A> sim_;
	private final Representer<S, X> repr_;
	private final ActionGenerator<S, JointAction<A>> actions_;
	private final MctsVisitor<S, X, A> visitor_;
	private final double c_;
	private final int episode_limit_;
	private final RandomGenerator rng_;
	private final EvaluationFunction<S, A> eval_;
	
	// TODO: Should be a parameter
	protected final double discount_ = 1.0;
	
	private boolean complete_ = false;
	private MutableStateNode<S, X, A> root_ = null;
	
	public UctSearch( final UndoSimulator<S, A> sim,
					  final Representer<S, X> repr,
					  final ActionGenerator<S, JointAction<A>> actions,
					  final double c, final int episode_limit,
					  final RandomGenerator rng,
					  final EvaluationFunction<S, A> eval,
					  final MctsVisitor<S, X, A> visitor )
	{
		sim_ = sim;
		repr_ = repr;
		actions_ = actions;
		c_ = c;
		episode_limit_ = episode_limit;
		rng_ = rng;
		eval_ = eval;
		visitor_ = visitor;
	}
	
	protected abstract MutableStateNode<S, X, A> createStateNode(
		final MutableActionNode<S, X, A> an, final X x, final int nagents, final int[] turn );
	
	protected abstract MutableStateNode<S, X, A> fetchStateNode(
		final MutableActionNode<S, X, A> an, final X x, final int nagents, final int[] turn );
	
	@Override
	public void run()
	{
		int episode_count = 0;
		while( episode_count++ < episode_limit_ ) {
			// FIXME: Remove the 'depth' parameter if we're not going to
			// pay attention to it.
			// FIXME: The way you're handling creating the root node is stupid.
			// It requires special checks for 'null' everyplace that state
			// nodes get created.
//			System.out.println( Arrays.toString( visit( null, Integer.MAX_VALUE, visitor_ ) ) );
			visit( null, Integer.MAX_VALUE, visitor_ );
		}
		
		complete_ = true;
	}
	
	/**
	 * To achieve our goal of having StateNodes with different backup rules
	 * be different types, we need to implement the recursive tree-building
	 * function somewhat "backwards". Instead of creating a new node and then
	 * visiting it recursively, we instead visit the last *ActionNode*, and
	 * create the appropriate StateNode subtype under it. To create the
	 * root node, call createSubtree() with 'an' = null.
	 * @param an
	 * @param depth
	 * @param visitor
	 * @return
	 */
	private double[] visit(
		final MutableActionNode<S, X, A> an, final int depth, final MctsVisitor<S, X, A> visitor )
	{
		final S s = sim_.state();
		final int[] turn = sim_.turn();
		final int nagents = sim_.nagents();
		
		final X x;
		if( an != null ) {
			visitor.treeAction( an.a(), s, turn );
			x = an.repr().encode( s );
		}
		else {
			visitor.startEpisode( s, nagents, turn );
			x = repr_.encode( s );
		}
		
		// FIXME: If your Representer maps states with different 'turn'
		// vectors to the same X, then the search can fail here with a
		// (spurious) assertion.
		// 1. touchLeafNode( an, x, [] ) is called in a terminal state,
		// 	  storing [0] for turn
		// 2. touchStateNode( an, x, [0] ) is called, not in a terminal
		//    state. This retrieves the LeafStateNode from before! The leaf
		//	  state node has turn == [], and we get a (spurious) error because
		//    we think we're in a terminal state.
		// This is a leak in the abstraction, since whose turn it is is
		// supposed to be Simulator's responsibility, not State. It's also
		// an error in design for UctSearch, since you shouldn't even be
		// able to take an action in a leaf state!
		if( sim_.isTerminalState() || visitor.halt() ) {
			final double[] r = sim_.reward();
			final MutableStateNode<S, X, A> sn = touchLeafNode( r, an, x, turn );
			return r;
		}
		else if( depth == 0 ) {
			final MutableStateNode<S, X, A> sn = touchStateNode( an, x, turn );
//			final double[] r = rollout( sn, rollout_depth_, visitor );
			final double[] r = eval_.evaluate( sim_ );
			visitor.checkpoint();
			return r;
		}
		else {
			final MutableStateNode<S, X, A> sn = touchStateNode( an, x, turn );
			// Sample below 'sn' only if this is the first visit
			if( sn.n() == 1 ) {
//				final double[] r = rollout( sn, rollout_depth_, visitor );
				final double[] r = eval_.evaluate( sim_ );
				sn.setVhat( r );
				visitor.checkpoint();
				return r;
			}
			else {
				final double[] r = sim_.reward();
				final ActionGenerator<S, JointAction<A>> action_gen = actions_.create();
				action_gen.setState( s, sim_.t(), turn );
				final MutableActionNode<S, X, A> sa = selectAction( sn, action_gen );
				sa.visit();
				sim_.takeAction( sa.a().create() );
				final double[] z = visit( sa, depth - 1, visitor );
				sa.updateQ( z );
				Fn.scalar_multiply_inplace( z, discount_ );
				Fn.vplus_inplace( r, z );
				sim_.untakeLastAction();
				return r;
			}
		}
	}
	
	private int singleAgent( final int[] turn )
	{
		if( turn.length == 1 ) {
			return turn[0];
		}
		else {
			System.out.println( "! turn.length = " + turn.length );
			throw new IllegalStateException( "Not designed for simultaneous moves right now!" );
		}
	}
	
	private MutableActionNode<S, X, A> selectAction(
		final MutableStateNode<S, X, A> sn, final ActionGenerator<S, JointAction<A>> actions )
	{
//		System.out.println( sn.token );
		assert( actions.size() > 0 );
		double max_value = -Double.MAX_VALUE;
		MutableActionNode<S, X, A> max_sa = null;
		while( actions.hasNext() ) {
			final JointAction<A> a = actions.next();
			final MutableActionNode<S, X, A> sa = requireActionNode( sn, a );
			if( sa.n() == 0 ) {
				max_sa = sa;
				break;
			}
			else {
				// TODO: Figure out how to generalize for simultaneous moves
//				System.out.println( Arrays.toString( sn.turn ) );
//				System.out.println( sn.getClass() );
//				System.out.println( sim_.isTerminalState() );
//				printTree( System.out );
				final double exploit = sa.q( singleAgent( sn.turn ) );
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
	
	private MutableStateNode<S, X, A> touchStateNode( final MutableActionNode<S, X, A> an, final X x, final int[] turn )
	{
		assert( turn.length > 0 );
		MutableStateNode<S, X, A> sn = null;
		if( an == null ) {
			if( root_ == null ) {
				sn = createStateNode( an, x, sim_.nagents(), turn );
				root_ = sn;
			}
			else {
				sn = root_;
			}
		}
		else {
			sn = fetchStateNode( an, x, sim_.nagents(), turn );
			if( sn == null ) {
				sn = createStateNode( an, x, sim_.nagents(), turn );
				an.attachSuccessor( x, turn, sn );
			}
			else {
				assert( !(sn instanceof LeafStateNode<?, ?, ?>) );
			}
		}
		sn.visit();
		return sn;
	}
	
	private MutableStateNode<S, X, A> touchLeafNode(
		final double[] v, final MutableActionNode<S, X, A> an, final X x, final int[] turn )
	{
		MutableStateNode<S, X, A> sn = null;
		if( an == null ) {
			if( root_ == null ) {
				sn = new LeafStateNode<S, X, A>( v, x, sim_.nagents(), turn );
				root_ = sn;
			}
			else {
				sn = root_;
			}
		}
		else {
			sn = an.getStateNode( x, turn );
			if( sn == null ) {
				sn = new LeafStateNode<S, X, A>( v, x, sim_.nagents(), turn );
				an.attachSuccessor( x, turn, sn );
			}
		}
		sn.visit();
		return sn;
	}
	
	private MutableActionNode<S, X, A> requireActionNode( final MutableStateNode<S, X, A> sn, final JointAction<A> a )
	{
		MutableActionNode<S, X, A> an = sn.getActionNode( a );
		if( an == null ) {
			an = new MutableActionNode<S, X, A>( a, sim_.nagents(), repr_.create() );
			sn.attachSuccessor( a, an );
		}
		return an;
	}
	
	public boolean isComplete()
	{
		return complete_;
	}

	@Override
	public StateNode<X, A> root()
	{
		return root_;
	}
	
	public void printTree( final PrintStream out )
	{
//		printStateNode( root_, 0, out );
		root().accept( new TreePrinter<X, A>() );
	}
}
