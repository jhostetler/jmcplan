/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.firegirl;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.abstraction.IndexRepresentation;

/**
 * @author jhostetler
 *
 */
public class FireGirlActionSetRepresenter implements Representer<FireGirlState, Representation<FireGirlState>>
{
	@Override
	public Representer<FireGirlState, Representation<FireGirlState>> create()
	{
		return new FireGirlActionSetRepresenter();
	}

	@Override
	public Representation<FireGirlState> encode( final FireGirlState s )
	{
		if( s.isTerminal() ) {
			return new IndexRepresentation<FireGirlState>( 0 );
		}
		else {
			return new IndexRepresentation<FireGirlState>( 1 );
		}
	}
}
