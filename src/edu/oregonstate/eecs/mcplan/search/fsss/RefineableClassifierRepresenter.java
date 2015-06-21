/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public abstract class RefineableClassifierRepresenter<S extends State, A extends VirtualConstructor<A>>
	extends ClassifierRepresenter<S, A>
{

	public RefineableClassifierRepresenter( final FsssModel<S, A> model, final FsssAbstraction<S, A> abstraction )
	{
		super( model, abstraction );
	}
	
	@Override
	public abstract RefineableClassifierRepresenter<S, A> create();
	
//	public abstract boolean isFullyRefined( final FsssAbstractActionNode<S, A> aan );
	
	/**
	 * Returns an opaque object representing a refinement, or null to indicate
	 * that no refinement is available.
	 * @param aan
	 * @return
	 */
	public abstract Object proposeRefinement( final FsssAbstractActionNode<S, A> aan );
	
	public abstract void refine( final FsssAbstractActionNode<S, A> aan, final Object proposal );
	
	/**
	 * Refine the ASN using the implementation's specific refinement mechanism.
	 * @param asn
	 */
	public abstract void refine( final DataNode<S, A> dn );
}
