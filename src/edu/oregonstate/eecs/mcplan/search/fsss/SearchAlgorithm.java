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
public interface SearchAlgorithm<S extends State, A extends VirtualConstructor<A>>
{
	public abstract FsssAbstractStateNode<S, A> root();

	public abstract void enableLogging();

	public abstract void run();

	public abstract int numRefinements();
}
