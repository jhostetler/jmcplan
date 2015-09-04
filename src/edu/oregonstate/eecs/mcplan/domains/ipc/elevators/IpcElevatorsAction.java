/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.elevators;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public final class IpcElevatorsAction implements VirtualConstructor<IpcElevatorsAction>
{
	public static enum Type
	{
		MoveCurrentDir, OpenDoorGoingUp, OpenDoorGoingDown, CloseDoor
	}
	
	public final Type[] actions;
	
	public IpcElevatorsAction( final Type... actions )
	{
		this.actions = actions;
	}
	
	@Override
	public IpcElevatorsAction create()
	{
		return new IpcElevatorsAction( actions );
	}
	
	@Override
	public String toString()
	{
		return "ElevatorsAction" + Arrays.toString( actions );
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		final IpcElevatorsAction that = (IpcElevatorsAction) obj;
		return Arrays.equals( actions, that.actions );
	}
	
	@Override
	public int hashCode()
	{
		return Arrays.hashCode( actions );
	}
}
