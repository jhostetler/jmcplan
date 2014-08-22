/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.io.PrintStream;

import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.ResetAdapter;
import edu.oregonstate.eecs.mcplan.sim.ResetSimulator;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Fn;


/**
 * UCT search for multiagent, general-reward domains.
 *
 * @param <S> State type
 * @param <X> State representation type
 * @param <A> Action type
 */
public abstract class AbstractUctSearch<S extends State, A extends VirtualConstructor<A>>
	implements Runnable, GameTree<FactoredRepresentation<S>, A>
{
	private static final Logger log = LoggerFactory.getLogger( UctSearch.class );
	
	public static final class Factory<S extends State, A extends VirtualConstructor<A>>
		implements GameTreeFactory<S, FactoredRepresentation<S>, A>
	{
		private final UndoSimulator<S, A> sim_;
		private final Representer<S, FactoredRepresentation<S>> repr_;
		private final ActionGenerator<S, JointAction<A>> actions_;
		private final double c_;
		private final int episode_limit_;
		private final RandomGenerator rng_;
		private final EvaluationFunction<S, A> eval_;
		private final BackupRule<FactoredRepresentation<S>, A> backup_;
		private final double[] default_value_;
		
		public Factory( final UndoSimulator<S, A> sim,
						  final Representer<S, FactoredRepresentation<S>> repr,
						  final ActionGenerator<S, JointAction<A>> actions,
						  final double c, final int episode_limit,
						  final RandomGenerator rng,
						  final EvaluationFunction<S, A> eval,
						  final BackupRule<FactoredRepresentation<S>, A> backup, final double[] default_value )
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
		public GameTree<FactoredRepresentation<S>, A> create( final MctsVisitor<S, A> visitor )
		{
			return new AbstractUctSearch<S, A>( new ResetAdapter<S, A>( sim_ ), repr_.create(), actions_, c_, episode_limit_,
										   rng_, eval_, visitor )
			{
				@Override
				protected MutableStateNode<S, FactoredRepresentation<S>, A> createStateNode(
					final MutableActionNode<S, FactoredRepresentation<S>, A> an,
					final FactoredRepresentation<S> x, final int nagents, final int[] turn,
					final ActionGenerator<S, JointAction<A>> action_gen )
				{
					return new DelegateStateNode<S, FactoredRepresentation<S>, A>( backup_, default_value_, x, nagents, turn, action_gen );
				}
				
				@Override
				protected MutableStateNode<S, FactoredRepresentation<S>, A> fetchStateNode(
					final MutableActionNode<S, FactoredRepresentation<S>, A> an,
					final FactoredRepresentation<S> x, final int nagents, final int[] turn )
				{
					for( final MutableStateNode<S, FactoredRepresentation<S>, A> agg : Fn.in( an.successors() ) ) {
						if( agg.token.equals( x ) ) {
							return agg;
						}
					}
					return null;
				}
			};
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final ResetSimulator<S, A> sim_;
	private final Representer<S, FactoredRepresentation<S>> repr_;
	private final ActionGenerator<S, JointAction<A>> actions_;
	private final MctsVisitor<S, A> visitor_;
	private final double c_;
	private final int episode_limit_;
	private final RandomGenerator rng_;
	private final EvaluationFunction<S, A> eval_;
	
	// TODO: Should be a parameter
	protected final double discount_ = 1.0;
	
	private boolean complete_ = false;
	private MutableStateNode<S, FactoredRepresentation<S>, A> root_ = null;
	
	private final int max_depth_ = Integer.MAX_VALUE;
	
	public AbstractUctSearch( final ResetSimulator<S, A> sim,
					  final Representer<S, FactoredRepresentation<S>> repr,
					  final ActionGenerator<S, JointAction<A>> actions,
					  final double c, final int episode_limit,
					  final RandomGenerator rng,
					  final EvaluationFunction<S, A> eval,
					  final MctsVisitor<S, A> visitor )
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
	
	protected abstract MutableStateNode<S, FactoredRepresentation<S>, A> createStateNode(
		final MutableActionNode<S, FactoredRepresentation<S>, A> an, final FactoredRepresentation<S> x, final int nagents, final int[] turn,
		final ActionGenerator<S, JointAction<A>> action_gen );
	
	protected abstract MutableStateNode<S, FactoredRepresentation<S>, A> fetchStateNode(
		final MutableActionNode<S, FactoredRepresentation<S>, A> an, final FactoredRepresentation<S> x, final int nagents, final int[] turn );
	
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
			visit( null, 0, visitor_ );
			sim_.reset();
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
		final MutableActionNode<S, FactoredRepresentation<S>, A> an, final int depth, final MctsVisitor<S, A> visitor )
	{
//		System.out.println( "visit()" );
		final S s = sim_.state();
		final int[] turn = sim_.turn();
		final int nagents = sim_.nagents();
		
		final FactoredRepresentation<S> x;
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
		if( sim_.isTerminalState() ) {
			final double[] r = sim_.reward();
			final MutableStateNode<S, FactoredRepresentation<S>, A> sn = touchLeafNode( r, an, x, turn );
			sn.updateR( r );
			return r;
		}
		else {
			final MutableStateNode<S, FactoredRepresentation<S>, A> sn = touchStateNode( an, x, turn );
			final double[] r = sim_.reward();
			sn.updateR( r );
			
			// If we've reached the fringe of the tree, use the evaluation function
			if( sn.n() == 1 || depth == max_depth_ ) {
				final double[] v = eval_.evaluate( sim_ );
				visitor.checkpoint();
				return v;
			}
			else {
				// Sample below 'sn'
				final MutableActionNode<S, FactoredRepresentation<S>, A> sa = selectAction( sn, s, sim_.t(), turn );
				sa.visit();
				sim_.takeAction( sa.a().create() );
				final double[] z = visit( sa, depth + 1, visitor );
				sa.updateQ( z );
				Fn.scalar_multiply_inplace( z, discount_ );
				Fn.vplus_inplace( r, z );
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
	
	private MutableActionNode<S, FactoredRepresentation<S>, A>
		selectAction( final MutableStateNode<S, FactoredRepresentation<S>, A> sn, final S s, final long t, final int[] turn )
	{
//		System.out.println( sn.token );
		assert( !(sn instanceof LeafStateNode<?, ?, ?>) );
		
		double max_value = -Double.MAX_VALUE;
		MutableActionNode<S, FactoredRepresentation<S>, A> max_sa = null;
		sn.action_gen_.setState( s, t, turn );
		while( sn.action_gen_.hasNext() ) {
			final JointAction<A> a = sn.action_gen_.next();
			final MutableActionNode<S, FactoredRepresentation<S>, A> an = sn.successor_map().get( a );
			if( an == null ) {
				final MutableActionNode<S, FactoredRepresentation<S>, A> sa = requireActionNode( sn, a );
				return sa;
			}
			else {
				final double exploit = an.q( singleAgent( sn.turn ) );
				final double explore = c_ * Math.sqrt( Math.log( sn.n() ) / an.n() );
				final double v = explore + exploit;
				if( v > max_value ) {
					max_sa = an;
					max_value = v;
				}
			}
		}
		assert( max_sa != null );
		return max_sa;
		
		
		// If there are any actions left in action_gen, they have not been
		// tried once yet, so we try them first.
//		if( sn.action_gen_.hasNext() ) {
//			final JointAction<A> a = sn.action_gen_.next();
//			final MutableActionNode<S, X, A> sa = requireActionNode( sn, a );
//			return sa;
//		}
//		else {
//			// Otherwise, choose using UCB rule
//			double max_value = -Double.MAX_VALUE;
//			MutableActionNode<S, X, A> max_sa = null;
//			for( final MutableActionNode<S, X, A> sa : Fn.in( sn.successors() ) ) {
//				final double exploit = sa.q( singleAgent( sn.turn ) );
//				final double explore = c_ * Math.sqrt( Math.log( sn.n() ) / sa.n() );
//				final double v = explore + exploit;
//				if( v > max_value ) {
//					max_sa = sa;
//					max_value = v;
//				}
//			}
//			assert( max_sa != null );
//			return max_sa;
//		}
		
		
//		assert( actions.size() > 0 );
//		double max_value = -Double.MAX_VALUE;
//		MutableActionNode<S, X, A> max_sa = null;
//		while( actions.hasNext() ) {
//			final JointAction<A> a = actions.next();
//			final MutableActionNode<S, X, A> sa = requireActionNode( sn, a );
//			if( sa.n() == 0 ) {
//				max_sa = sa;
//				break;
//			}
//			else {
//				// TODO: Figure out how to generalize for simultaneous moves
////				System.out.println( Arrays.toString( sn.turn ) );
////				System.out.println( sn.getClass() );
////				System.out.println( sim_.isTerminalState() );
////				printTree( System.out );
//				final double exploit = sa.q( singleAgent( sn.turn ) );
//				final double explore = c_ * Math.sqrt( Math.log( sn.n() ) / sa.n() );
//				final double v = explore + exploit;
//				if( v > max_value ) {
//					max_sa = sa;
//					max_value = v;
//				}
//			}
//		}
//		return max_sa;
	}
	
	private MutableStateNode<S, FactoredRepresentation<S>, A> touchStateNode(
			final MutableActionNode<S, FactoredRepresentation<S>, A> an, final FactoredRepresentation<S> x, final int[] turn )
	{
		assert( turn.length > 0 );
		MutableStateNode<S, FactoredRepresentation<S>, A> sn = null;
		if( an == null ) {
			if( root_ == null ) {
				final ActionGenerator<S, JointAction<A>> action_gen = actions_.create();
				action_gen.setState( sim_.state(), sim_.t(), turn );
				sn = createStateNode( an, x, sim_.nagents(), turn, action_gen );
				root_ = sn;
			}
			else {
				sn = root_;
			}
		}
		else {
			sn = fetchStateNode( an, x, sim_.nagents(), turn );
			if( sn == null ) {
				final ActionGenerator<S, JointAction<A>> action_gen = actions_.create();
				action_gen.setState( sim_.state(), sim_.t(), turn );
				sn = createStateNode( an, x, sim_.nagents(), turn, action_gen );
				an.attachSuccessor( x, turn, sn );
			}
			else {
				assert( !(sn instanceof LeafStateNode<?, ?, ?>) );
			}
		}
		sn.visit();
		return sn;
	}
	
	private MutableStateNode<S, FactoredRepresentation<S>, A> touchLeafNode(
		final double[] v, final MutableActionNode<S, FactoredRepresentation<S>, A> an, final FactoredRepresentation<S> x, final int[] turn )
	{
		MutableStateNode<S, FactoredRepresentation<S>, A> sn = null;
		if( an == null ) {
			if( root_ == null ) {
				sn = new LeafStateNode<S, FactoredRepresentation<S>, A>( v, x, sim_.nagents(), turn );
				root_ = sn;
			}
			else {
				sn = root_;
			}
		}
		else {
			sn = an.getStateNode( x, turn );
			if( sn == null ) {
				sn = new LeafStateNode<S, FactoredRepresentation<S>, A>( v, x, sim_.nagents(), turn );
				an.attachSuccessor( x, turn, sn );
			}
		}
		sn.visit();
		return sn;
	}
	
	private MutableActionNode<S, FactoredRepresentation<S>, A> requireActionNode(
			final MutableStateNode<S, FactoredRepresentation<S>, A> sn, final JointAction<A> a )
	{
		MutableActionNode<S, FactoredRepresentation<S>, A> an = sn.getActionNode( a );
		if( an == null ) {
			// FIXME: I'm *not* spawning a new 'repr_' so that we can do
			// proper experiments with random noise for the AAAI paper.
//			an = new MutableActionNode<S, X, A>( a, sim_.nagents(), repr_ );
			
			// This is how we normally want to do it
			an = new MutableActionNode<S, FactoredRepresentation<S>, A>( a, sim_.nagents(), repr_.create() );
			sn.attachSuccessor( a, an );
		}
		return an;
	}
	
	public boolean isComplete()
	{
		return complete_;
	}

	@Override
	public StateNode<FactoredRepresentation<S>, A> root()
	{
		return root_;
	}
	
	public void printTree( final PrintStream out )
	{
//		printStateNode( root_, 0, out );
		root().accept( new TreePrinter<FactoredRepresentation<S>, A>() );
	}
}
