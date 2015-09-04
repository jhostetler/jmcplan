/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.sailing;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.abstraction.IndexRepresentation;

/**
 * @author jhostetler
 *
 */
public class SailingActionSetRepresenter implements Representer<SailingState, Representation<SailingState>>
{
	@Override
	public Representer<SailingState, Representation<SailingState>> create()
	{
		return new SailingActionSetRepresenter();
	}

	@Override
	public Representation<SailingState> encode( final SailingState s )
	{
		if( s.isTerminal() ) {
			return new IndexRepresentation<SailingState>( 0 );
		}
		else {
			return new IndexRepresentation<SailingState>( 1 );
		}
	}
}
