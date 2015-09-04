/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.crossing;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public enum IpcCrossingAction implements VirtualConstructor<IpcCrossingAction>
{
	North, South, East, West;

	@Override
	public IpcCrossingAction create()
	{
		return this;
	}
}
