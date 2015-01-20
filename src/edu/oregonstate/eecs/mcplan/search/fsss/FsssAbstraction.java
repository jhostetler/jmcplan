/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;

/**
 * @author jhostetler
 *
 */
public abstract class FsssAbstraction<S, A>
{
	public abstract Representer<S, ? extends Representation<S>> createRepresenter();
}
