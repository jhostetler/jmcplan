package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * Always refines the subtree with greatest upper bound.
 *
 * @param <S>
 * @param <A>
 */
public class BestFirstRefinementOrder<S extends State, A extends VirtualConstructor<A>>
	extends RefinementOrderBase<S, A>
{
	public static class Factory<S extends State, A extends VirtualConstructor<A>>
		implements RefinementOrderBase.Factory<S, A>
	{
		private final SubtreeRefinementOrder.Factory<S, A> subtree_factory;
		
		public Factory( final SubtreeRefinementOrder.Factory<S, A> subtree_factory )
		{
			this.subtree_factory = subtree_factory;
		}
		
		@Override
		public String toString()
		{
			return "BestFirst(" + subtree_factory + ")";
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
			assert( subtrees.size() > 1 );
			return new BestFirstRefinementOrder<S, A>( root, subtrees );
		}
	}
	
	// -----------------------------------------------------------------------
	
	public BestFirstRefinementOrder( final FsssAbstractStateNode<S, A> root,
									 final ArrayList<SubtreeRefinementOrder<S, A>> subtrees )
	{
		super( root, subtrees );
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
		double max_U = -Double.MAX_VALUE;
		SubtreeRefinementOrder<S, A> max_subtree = null;
		for( final SubtreeRefinementOrder<S, A> t : subtrees.values() ) {
			if( t.rootAction().a().equals( astar.a() ) ) {
				continue;
			}
			else {
				assert( !t.isClosed() );
				// TODO: Randomize subtree selection here
				final double Ui = t.rootAction().U();
				if( Ui > max_U ) {
					max_U = Ui;
					max_subtree = t;
				}
			}
		}
		return max_subtree;
	}
}
