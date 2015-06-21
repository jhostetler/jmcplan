/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public abstract class RefinementOrderBase<S extends State, A extends VirtualConstructor<A>>
	implements RefinementOrder<S, A>
{
	public static interface Factory<S extends State, A extends VirtualConstructor<A>>
	{
		public abstract RefinementOrderBase<S, A> create(
			final FsssParameters parameters, final FsssModel<S, A> model, final FsssAbstractStateNode<S, A> root );
	}
	
	protected final FsssAbstractStateNode<S, A> root;
	protected final LinkedHashMap<FsssAbstractActionNode<S, A>, SubtreeRefinementOrder<S, A>> subtrees;
	protected final Set<SubtreeRefinementOrder<S, A>> inactive = new HashSet<SubtreeRefinementOrder<S, A>>();
	
	boolean closed = false;
	
	public RefinementOrderBase( final FsssAbstractStateNode<S, A> root,
								final ArrayList<SubtreeRefinementOrder<S, A>> subtrees )
	{
		this.root = root;
		this.subtrees = new LinkedHashMap<FsssAbstractActionNode<S, A>, SubtreeRefinementOrder<S, A>>();
		for( final SubtreeRefinementOrder<S, A> subtree : subtrees ) {
			this.subtrees.put( subtree.rootAction(), subtree );
		}
	}
	
	protected abstract SubtreeRefinementOrder<S, A> chooseSubtree();
	
	public void addStateNodeToSubtree( final FsssAbstractActionNode<S, A> root_action,
									   final FsssAbstractStateNode<S, A> asn )
	{
		final SubtreeRefinementOrder<S, A> subtree = subtrees.get( root_action );
		if( subtree == null ) {
			System.out.println( "\t!subtree closed: " + root_action );
			FsssTest.printTree( root, System.out, 2 );
		}
		subtree.addNewStateNode( asn );
		if( inactive.contains( subtree ) ) {
			if( subtree.isActive() ) {
				inactive.remove( subtree );
			}
		}
	}
	
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
			if( subtrees.isEmpty() ) {
				closed = true;
				break;
			}
			
			final SubtreeRefinementOrder<S, A> subtree = chooseSubtree();
			if( subtree != null ) {
				assert( !subtree.isClosed() );
				assert( subtree.isActive() );
				assert( !inactive.contains( subtree ) );
				subtree.refine();
				if( subtree.isClosed() ) {
					final boolean check = subtrees.values().remove( subtree );
					assert( check );
				}
				else {
					if( !subtree.isActive() ) {
						inactive.add( subtree );
					}
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
