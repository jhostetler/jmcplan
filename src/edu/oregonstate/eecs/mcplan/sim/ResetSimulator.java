/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public interface ResetSimulator<S, A extends VirtualConstructor<A>> extends Simulator<S, A>
{
	/**
	 * Resets the simulator to the initial state.
	 */
	public void reset();
}
