/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.pats;

import edu.oregonstate.eecs.mcplan.Representation;

/**
 * @author jhostetler
 *
 */
public interface StateAbstraction<S>
{
	public abstract Representation<S> encode( final S s );
	public abstract void refine();
}
