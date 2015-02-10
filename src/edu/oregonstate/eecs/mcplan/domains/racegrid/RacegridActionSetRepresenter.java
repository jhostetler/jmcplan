/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racegrid;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.abstraction.IndexRepresentation;

/**
 * @author jhostetler
 *
 */
public class RacegridActionSetRepresenter implements Representer<RacegridState, Representation<RacegridState>>
{
	@Override
	public Representer<RacegridState, Representation<RacegridState>> create()
	{
		return new RacegridActionSetRepresenter();
	}

	@Override
	public Representation<RacegridState> encode( final RacegridState s )
	{
		if( s.isTerminal() ) {
			return new IndexRepresentation<RacegridState>( 0 );
		}
		else {
			return new IndexRepresentation<RacegridState>( 1 );
		}
	}
}
