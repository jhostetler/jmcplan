/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;
import java.util.Iterator;

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
		implements RefinementOrderBase.Factory<S, A>
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
		public RefinementOrderBase<S, A> create( final FsssParameters parameters, final FsssModel<S, A> model,
									   final FsssAbstractStateNode<S, A> root )
		{
			final ArrayList<SubtreeRefinementOrder<S, A>> subtrees
				= new ArrayList<SubtreeRefinementOrder<S, A>>();
			for( final FsssAbstractActionNode<S, A> aan : root.successors() ) {
				subtrees.add( subtree_factory.create( parameters, model, aan ) );
			}
			assert( subtrees.size() == root.nsuccessors() );
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
		if( rng.nextDouble() > epsilon ) {
			// Greedy choice of the *2nd-best* action
			final FsssAbstractActionNode<S, A> aan = root.astar_random();
			assert( aan != null );
			
			final ArrayList<SubtreeRefinementOrder<S, A>> astar = new ArrayList<SubtreeRefinementOrder<S, A>>();
			double ustar = -Double.MAX_VALUE;
			for( final SubtreeRefinementOrder<S, A> subtree : subtrees.values() ) {
				final FsssAbstractActionNode<S, A> ai = subtree.rootAction();
				if( ai == aan ) {
					// Skip the optimal subtree
					continue;
				}
				final double u = ai.U();
				if( u > ustar ) {
					ustar = u;
					astar.clear();
					astar.add( subtree );
				}
				else if( u >= ustar ) {
					astar.add( subtree );
				}
			}

			final SubtreeRefinementOrder<S, A> subtree = astar.get( rng.nextInt( astar.size() ) );
//			System.out.println( "\tEpsilonGreedyRefinementOrder: greedy choice " + subtree.rootAction() );
			return subtree;
		}
		else {
			// Random choice
			final int choice = rng.nextInt( subtrees.size() );
			final Iterator<SubtreeRefinementOrder<S, A>> itr = subtrees.values().iterator();
			for( int i = 0; i < choice; ++i ) {
				itr.next();
			}
			final SubtreeRefinementOrder<S, A> subtree = itr.next();
//			System.out.println( "\tEpsilonGreedyRefinementOrder: random subtree " + subtree.rootAction() );
			return subtree;
		}
	}
}
