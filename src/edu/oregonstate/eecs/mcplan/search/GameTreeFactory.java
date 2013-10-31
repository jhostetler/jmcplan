/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public interface GameTreeFactory<S, X extends Representation<S>, A extends VirtualConstructor<A>>
{
	public abstract GameTree<X, A> create( final MctsVisitor<S, X, A> visitor );
}
