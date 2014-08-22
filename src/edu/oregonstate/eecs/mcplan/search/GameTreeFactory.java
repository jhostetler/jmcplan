/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public interface GameTreeFactory<S, A extends VirtualConstructor<A>>
{
	public abstract GameTree<S, A> create( final MctsVisitor<S, A> visitor );
}
