/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * Represents a node in a game tree. The 'visit()' method implements half of
 * the "double dispatch" pattern along with the methods in GameTreeVisitor,
 * allowing heterogeneous nodes to be traversed polymorphically.
 */
public interface GameTreeNode<S, A extends VirtualConstructor<A>>
{
	/**
	 * The implementation should call 'visit()' on the visitor with 'this'
	 * as the argument.
	 * @param visitor
	 */
	public abstract void accept( final GameTreeVisitor<S, A> visitor );
	
	public abstract Generator<? extends GameTreeNode<S, A>> successors();
}
