/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public interface GameTreeFactory<S, F extends Representer<S, F>, A extends VirtualConstructor<A>>
{
	public abstract GameTree<Representation<S, F>, A> create( final MctsVisitor<S, A> visitor );
}
