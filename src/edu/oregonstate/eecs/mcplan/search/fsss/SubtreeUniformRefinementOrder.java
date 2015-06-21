/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.HashSet;
import java.util.Set;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.RandomAccessHashSet;

/**
 * Chooses an abstraction to refine within the subtree uniformly at random.
 * 
 * Compared to breadth-first order, there are some complications. Namely, a
 * node may become fully refined before all of its ancestors are fully refined.
 * If its ancestors are subsequently refined, additional samples may be added
 * which would cause fully refined descendents to no longer be fully refined.
 * Thus, we maintain an Active set and an Inactive set. Active contains all
 * nodes that are not fully refined. Inactive contains nodes that are fully
 * refined but that might become not fully refined in the future. A node
 * is closed (ie. removed from *both* sets) if and only if it is fully refined
 * and all of its ancestors are closed.
 * 
 * If a node is refined, we add all of its descendents to Active. This is
 * potentially inefficient, because many of its descendents might still be
 * inactive. To improve the efficiency, we would need upSample() to return a
 * list of all nodes that had samples added (including newly-created nodes).
 * 
 * @author jhostetler
 *
 */
public class SubtreeUniformRefinementOrder<S extends State, A extends VirtualConstructor<A>>
	extends SubtreeRefinementOrder<S, A>
{
	public static class Factory<S extends State, A extends VirtualConstructor<A>>
		implements SubtreeRefinementOrder.Factory<S, A>
	{
		@Override
		public SubtreeRefinementOrder<S, A> create( final FsssParameters parameters,
				final FsssModel<S, A> model, final FsssAbstractActionNode<S, A> root )
		{
			return new SubtreeUniformRefinementOrder<S, A>( parameters, model, root );
		}
		
		@Override
		public String toString()
		{
			return "SubtreeUniform";
		}
	}
	
	// -----------------------------------------------------------------------
	
	protected final FsssParameters parameters;
	protected final FsssModel<S, A> model;
	protected final FsssAbstractActionNode<S, A> root_action;
	
	private final RandomAccessHashSet<FsssAbstractActionNode<S, A>> active_set
		= new RandomAccessHashSet<FsssAbstractActionNode<S, A>>();
	private final Set<FsssAbstractActionNode<S, A>> inactive_set
		= new HashSet<FsssAbstractActionNode<S, A>>();
	
	public SubtreeUniformRefinementOrder( final FsssParameters parameters,
											   final FsssModel<S, A> model,
											   final FsssAbstractActionNode<S, A> root_action )
	{
		this.parameters = parameters;
		this.model = model;
		this.root_action = root_action;
		
		populateActiveSet( root_action );
	}
	
	/**
	 * Ensures that 'aan' and all of its AAN descendents are in the active
	 * set and are not in the inactive set.
	 * @param aan
	 */
	private void populateActiveSet( final FsssAbstractActionNode<S, A> aan )
	{
		active_set.add( aan );
		final boolean b = inactive_set.remove( aan );
		if( b ) {
			System.out.println( "\tReactivating: " + aan );
		}
		for( final FsssAbstractStateNode<S, A> asn : aan.successors() ) {
			for( final FsssAbstractActionNode<S, A> aan_prime : asn.successors() ) {
				populateActiveSet( aan_prime );
			}
		}
	}
	
	private void forgetSubtreeNodes( final FsssAbstractActionNode<S, A> aan )
	{
//		System.out.println( "\tForgetting: " + aan );
		active_set.remove( aan );
		inactive_set.remove( aan );
		for( final FsssAbstractStateNode<S, A> asn : aan.successors() ) {
			for( final FsssAbstractActionNode<S, A> aan_prime : asn.successors() ) {
				forgetSubtreeNodes( aan_prime );
			}
		}
	}
	
	@Override
	public boolean isClosed()
	{
		return active_set.isEmpty() && inactive_set.isEmpty();
	}
	
	@Override
	public boolean isActive()
	{
		return !active_set.isEmpty();
	}

	@Override
	public void refine()
	{
		while( true ) {
			if( active_set.isEmpty() ) {
//				assert( inactive_set.isEmpty() );
				return;
			}
			
			final int i = model.rng().nextInt( active_set.size() );
			final FsssAbstractActionNode<S, A> aan = active_set.get( i );
			
			final RefineableClassifierRepresenter<S, A> repr = (RefineableClassifierRepresenter<S, A>) aan.repr;
			final Object proposal = repr.proposeRefinement( aan );
			
			if( proposal == null ) {
				// No refinement to do. You can only close the node if
				// all of its ancestors are closed. It is sufficient to check
				// if its parent is closed, because:
				//		1. The first time the root node is not refined, its
				//		   parent is trivially closed, thus all of its
				//		   predecessors are closed and the root becomes closed.
				//		2. Inductively, if the first time a node at depth d
				//		   is not refined its parent is closed, then all of its
				//		   predecessors are closed.
				final FsssAbstractActionNode<S, A> aan_pred = aan.predecessor.predecessor;
				if( active_set.contains( aan_pred ) || inactive_set.contains( aan_pred ) ) {
//					System.out.println( "\tSubtreeUniform: Moving to inactive " + aan );
					active_set.remove( aan );
					inactive_set.add( aan );
				}
				else {
//					System.out.println( "\tSubtreeUniform: Closing " + aan );
					active_set.remove( aan );
				}
				continue;
			}
			
			// We're going to use all new instances for the new subtree, so
			// we remove the old instances from the maps.
			forgetSubtreeNodes( aan );
//			for( final FsssAbstractActionNode<S, A> active : active_set ) {
//				System.out.println( "\tUnforgotten: " + active );
//			}
			
			repr.refine( aan, proposal );
			upSample( aan, parameters );
			backupToRoot( aan );
			
			// Move all descendents to the Active set, since even the ones
			// that had been inactive might have been reactivated.
			populateActiveSet( aan );
//			for( final FsssAbstractActionNode<S, A> active : active_set ) {
//				System.out.println( "\tActive: " + active );
//			}
			
			break;
		}
	}

	@Override
	public FsssAbstractActionNode<S, A> rootAction()
	{
		return root_action;
	}

	@Override
	public void addNewStateNode( final FsssAbstractStateNode<S, A> asn )
	{
		assert( asn.predecessor != null );
		final boolean check = active_set.add( asn.predecessor );
//		assert( !check );
		inactive_set.remove( asn.predecessor );
		
		System.out.println( "\tSubtreeUniform: Added new ASN: " + asn );
	}
}
