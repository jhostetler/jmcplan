/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.tamarisk;

import edu.oregonstate.eecs.mcplan.Action;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public final class IpcTamariskAction implements Action<IpcTamariskState>, VirtualConstructor<IpcTamariskAction>
{
	public final IpcTamariskActionSet type;
	public final int reach;
	
	public IpcTamariskAction( final IpcTamariskActionSet type, final int reach )
	{
		this.type = type;
		this.reach = reach;
	}
	
	@Override
	public IpcTamariskAction create()
	{
		return this;
	}
	
	@Override
	public String toString()
	{
		return type.toString() + "[" + reach + "]";
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		final IpcTamariskAction that = (IpcTamariskAction) obj;
		return type == that.type && reach == that.reach;
	}
	
	@Override
	public int hashCode()
	{
		return type.hashCode() ^ (7 *reach);
	}

	@Override
	public void doAction( final IpcTamariskState s )
	{
		throw new UnsupportedOperationException();
	}
}
