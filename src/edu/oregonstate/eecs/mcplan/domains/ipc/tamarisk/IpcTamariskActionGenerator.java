/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.tamarisk;

import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class IpcTamariskActionGenerator extends Generator<IpcTamariskAction>
{
	private final int Nreaches;
	private int reach = 0;
	private int i = 0;
	
	public IpcTamariskActionGenerator( final IpcTamariskParameters params )
	{
		Nreaches = params.Nreaches;
	}

	@Override
	public boolean hasNext()
	{
		// +1 to include Nothing action
		return i < (2*Nreaches + 1);
	}

	@Override
	public IpcTamariskAction next()
	{
		final IpcTamariskAction a;
		if( i == 0 ) {
			a = IpcTamariskActionSet.Nothing.create( 0 );
		}
		else if( (i & 1) == 1 ) { // Start with == 1 because i > 0
			a = IpcTamariskActionSet.Eradicate.create( reach );
		}
		else {
			a = IpcTamariskActionSet.Restore.create( reach );
			reach += 1;
		}
		i += 1;
		return a;
	}
}
