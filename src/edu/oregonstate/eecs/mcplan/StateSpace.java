/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public abstract class StateSpace<S>
{
	public abstract int cardinality();
	public abstract boolean isFinite();
	public abstract boolean isCountable();
	
	/**
	 * Returns a generator for the state space
	 * @return
	 */
	public abstract Generator<S> generator();
}
