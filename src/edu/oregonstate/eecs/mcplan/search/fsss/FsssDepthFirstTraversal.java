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
public abstract class FsssDepthFirstTraversal<S extends State, A extends VirtualConstructor<A>>
{
	public final void traverse( final FsssAbstractStateNode<S, A> asn )
	{
		final boolean recurse = visit( asn );
		if( recurse ) {
			for( final FsssAbstractActionNode<S, A> aan : asn.successors() ) {
				traverse( aan );
			}
		}
	}
	
	public final void traverse( final FsssAbstractActionNode<S, A> aan )
	{
		final boolean recurse = visit( aan );
		if( recurse ) {
			for( final FsssAbstractStateNode<S, A> asn : aan.successors() ) {
				traverse( asn );
			}
		}
	}
	
	protected boolean visit( final FsssAbstractStateNode<S, A> asn )
	{ return true; }
	
	protected boolean visit( final FsssAbstractActionNode<S, A> aan )
	{ return true; }
}
