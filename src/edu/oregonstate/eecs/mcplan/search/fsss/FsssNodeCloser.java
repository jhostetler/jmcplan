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
public final class FsssNodeCloser<S extends State, A extends VirtualConstructor<A>>
	extends FsssDepthFirstTraversal<S, A>
{
	@Override
	protected boolean visit( final FsssAbstractStateNode<S, A> asn )
	{
		if( !asn.isClosed() ) {
			if( asn.isReadyToClose() ) {
				asn.close();
				return true;
			}
			else {
				// Stop traversal at the first non-closed node
				return false;
			}
		}
		else {
			return true;
		}
	}
}
