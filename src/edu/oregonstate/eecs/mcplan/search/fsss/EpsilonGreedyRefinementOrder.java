/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class EpsilonGreedyRefinementOrder<S extends State, A extends VirtualConstructor<A>>
	extends RefinementOrderBase<S, A>
{
	public static class Factory<S extends State, A extends VirtualConstructor<A>>
		implements RefinementOrder.Factory<S, A>
	{
		private final RandomGenerator rng;
		private final double epsilon;
		private final SubtreeRefinementOrder.Factory<S, A> subtree_factory;
		
		public Factory( final RandomGenerator rng, final double epsilon,
						final SubtreeRefinementOrder.Factory<S, A> subtree_factory )
		{
			this.rng = rng;
			this.epsilon = epsilon;
			this.subtree_factory = subtree_factory;
		}
		
		@Override
		public String toString()
		{
			return "EpsilonGreedy(" + epsilon + "; " + subtree_factory + ")";
		}
	
		@Override
		public RefinementOrder<S, A> create( final FsssParameters parameters, final FsssModel<S, A> model,
									   final FsssAbstractStateNode<S, A> root )
		{
			final ArrayList<SubtreeRefinementOrder<S, A>> subtrees
				= new ArrayList<SubtreeRefinementOrder<S, A>>();
			for( final FsssAbstractActionNode<S, A> aan : root.successors() ) {
				subtrees.add( subtree_factory.create( parameters, model, aan ) );
			}
			assert( subtrees.size() > 1 );
			return new EpsilonGreedyRefinementOrder<S, A>( rng, epsilon, root, subtrees );
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final RandomGenerator rng;
	private final double epsilon;
	
	public EpsilonGreedyRefinementOrder( final RandomGenerator rng, final double epsilon,
									   final FsssAbstractStateNode<S, A> root,
									   final ArrayList<SubtreeRefinementOrder<S, A>> subtrees )
	{
		super( root, subtrees );
		this.rng = rng;
		this.epsilon = epsilon;
	}
	
	@Override
	public boolean isClosed()
	{
		return closed;
	}

	@Override
	protected SubtreeRefinementOrder<S, A> chooseSubtree()
	{
		final FsssAbstractActionNode<S, A> astar = root.astar();
		int astar_idx = Integer.MAX_VALUE; // This value ensures astar_idx > tree_idx if astar subtree is closed
		double max_U = -Double.MAX_VALUE;
		int max_idx = -1;
		for( int i = 0; i < subtrees.size(); ++i ) {
			final SubtreeRefinementOrder<S, A> t = subtrees.get( i );
			if( t.rootAction().a().equals( astar.a() ) ) {
				astar_idx = i;
				continue;
			}
			else {
				assert( !t.isClosed() );
				// TODO: Randomize subtree selection here
				final double Ui = t.rootAction().U();
				if( Ui > max_U ) {
					max_U = Ui;
					max_idx = i;
				}
			}
		}
		final int tree_idx;
		if( rng.nextDouble() < epsilon ) {
			// Greedy choice
			tree_idx = max_idx;
		}
		else {
			// Uniform random choice
			final int candidate = rng.nextInt( subtrees.size() - 1 );
			// Adjust index to skip optimal subtree
			tree_idx = (candidate < astar_idx ? candidate : candidate + 1);
		}
		final SubtreeRefinementOrder<S, A> selected_subtree	= subtrees.get( tree_idx );
		return selected_subtree;
	}
}
