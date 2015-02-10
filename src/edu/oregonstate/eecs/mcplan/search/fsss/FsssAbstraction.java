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
public abstract class FsssAbstraction<S extends State, A extends VirtualConstructor<A>>
{
	public abstract ClassifierRepresenter<S, A> createRepresenter();
}
