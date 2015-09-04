/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.elevators;

import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public final class IpcElevatorsActionGenerator extends Generator<IpcElevatorsAction>
{
	private final int[] idx;
	private boolean has_next = true;
	
	public IpcElevatorsActionGenerator( final IpcElevatorsParameters params )
	{
		idx = new int[params.Nelevators];
	}
	
	@Override
	public boolean hasNext()
	{
		return has_next;
	}

	@Override
	public IpcElevatorsAction next()
	{
		final IpcElevatorsAction.Type[] sig = new IpcElevatorsAction.Type[idx.length];
		for( int t = 0; t < idx.length; ++t ) {
			sig[t] = IpcElevatorsAction.Type.values()[idx[t]];
		}
		final IpcElevatorsAction a = new IpcElevatorsAction( sig );
		int i = 0;
		while( i < idx.length && idx[i] == IpcElevatorsAction.Type.values().length - 1 ) {
			idx[i] = 0;
			++i;
		}
		if( i == idx.length ) {
			has_next = false;
		}
		else {
			idx[i] += 1;
		}
		return a;
	}

}
