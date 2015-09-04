/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.crossing;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.abstraction.IndexRepresentation;

/**
 * @author jhostetler
 *
 */
public class IpcCrossingActionSetRepresenter implements Representer<IpcCrossingState, Representation<IpcCrossingState>>
{
	@Override
	public Representer<IpcCrossingState, Representation<IpcCrossingState>> create()
	{
		return new IpcCrossingActionSetRepresenter();
	}

	@Override
	public Representation<IpcCrossingState> encode( final IpcCrossingState s )
	{
		if( s.isTerminal() ) {
			return new IndexRepresentation<IpcCrossingState>( 0 );
		}
		else {
			return new IndexRepresentation<IpcCrossingState>( 1 );
		}
	}
}
