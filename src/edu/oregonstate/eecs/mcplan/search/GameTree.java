/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public interface GameTree<S, A extends VirtualConstructor<A>>
	extends Runnable
{
	public abstract StateNode<S, A> root();
}
