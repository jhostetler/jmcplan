/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public abstract class RefinementOrderBase<S extends State, A extends VirtualConstructor<A>>
	implements RefinementOrder<S, A>
{
	protected final FsssAbstractStateNode<S, A> root;
	protected final ArrayList<SubtreeRefinementOrder<S, A>> subtrees;
	
	boolean closed = false;
	
	public RefinementOrderBase( final FsssAbstractStateNode<S, A> root,
								final ArrayList<SubtreeRefinementOrder<S, A>> subtrees )
	{
		this.root = root;
		this.subtrees = subtrees;
	}
	
	protected abstract SubtreeRefinementOrder<S, A> chooseSubtree();
	
	@Override
	public boolean isClosed()
	{
		return closed;
	}
	
	/**
	 * Refines some non-optimal subtree. If isClosed() = true after a
	 * call to refine(), then no refinement was performed because the
	 * tree is fully refined.
	 */
	@Override
	public void refine()
	{
		assert( !isClosed() );
		// Find the non-optimal subtree with greatest upper bound U
		while( true ) {
			// Finished if only the optimal subtree remains
//			if( subtrees.size() == 1 ) {
			if( subtrees.isEmpty() ) {
				closed = true;
				break;
			}
			
			final SubtreeRefinementOrder<S, A> subtree = chooseSubtree();
			if( subtree != null ) {
				assert( !subtree.isClosed() );
				subtree.refine();
				if( subtree.isClosed() ) {
					final boolean check = subtrees.remove( subtree );
					assert( check );
				}
				else {
					// A refinement was performed
					break;
				}
			}
			else {
				// Tree is fully refined
				closed = true;
				break;
			}
		}
	}
}
