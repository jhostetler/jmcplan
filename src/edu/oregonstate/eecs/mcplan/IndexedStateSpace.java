/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * @author jhostetler
 *
 */
public abstract class IndexedStateSpace<S> extends StateSpace<S>
{
	/**
	 * Returns the unique, sequential, 0-index integer ID of the argument
	 * state.
	 * @param s
	 * @return
	 */
	public abstract int id( final S s );
}
