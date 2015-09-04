/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.crossing;

import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class IpcCrossingActionGenerator extends Generator<IpcCrossingAction>
{
	private int i = 0;
	
	@Override
	public boolean hasNext()
	{
		return i < IpcCrossingAction.values().length;
	}

	@Override
	public IpcCrossingAction next()
	{
		return IpcCrossingAction.values()[i++];
	}
}
