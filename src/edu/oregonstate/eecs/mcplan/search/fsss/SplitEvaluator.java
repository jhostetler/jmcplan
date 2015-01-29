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
public interface SplitEvaluator<S extends State, A extends VirtualConstructor<A>>
{
	public abstract double evaluateSplit( final FsssAbstractStateNode<S, A> asn,
										  final ArrayList<FsssStateNode<S, A>> U,
								  		  final ArrayList<FsssStateNode<S, A>> V );
}
