/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.elevators;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.abstraction.IndexRepresentation;

/**
 * @author jhostetler
 *
 */
public final class IpcElevatorsActionSetRepresenter
	implements Representer<IpcElevatorsState, Representation<IpcElevatorsState>>
{
	@Override
	public Representer<IpcElevatorsState, Representation<IpcElevatorsState>> create()
	{
		return new IpcElevatorsActionSetRepresenter();
	}

	@Override
	public Representation<IpcElevatorsState> encode( final IpcElevatorsState s )
	{
		if( s.isTerminal() ) {
			return new IndexRepresentation<IpcElevatorsState>( 0 );
		}
		else {
			return new IndexRepresentation<IpcElevatorsState>( 1 );
		}
	}
}
