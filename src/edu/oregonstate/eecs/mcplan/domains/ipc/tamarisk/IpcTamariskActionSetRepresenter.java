/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.tamarisk;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.abstraction.IndexRepresentation;

/**
 * @author jhostetler
 *
 */
public class IpcTamariskActionSetRepresenter implements Representer<IpcTamariskState, Representation<IpcTamariskState>>
{
	@Override
	public Representer<IpcTamariskState, Representation<IpcTamariskState>> create()
	{
		return new IpcTamariskActionSetRepresenter();
	}

	@Override
	public Representation<IpcTamariskState> encode( final IpcTamariskState s )
	{
		if( s.isTerminal() ) {
			return new IndexRepresentation<IpcTamariskState>( 0 );
		}
		else {
			return new IndexRepresentation<IpcTamariskState>( 1 );
		}
	}
}
