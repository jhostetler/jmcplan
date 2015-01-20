/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.spbj;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.abstraction.IndexRepresentation;

/**
 * @author jhostetler
 *
 */
public class SpBjActionSetRepresenter implements Representer<SpBjState, Representation<SpBjState>>
{
	@Override
	public Representer<SpBjState, Representation<SpBjState>> create()
	{
		return new SpBjActionSetRepresenter();
	}

	@Override
	public Representation<SpBjState> encode( final SpBjState s )
	{
		return new IndexRepresentation<SpBjState>( SpBjActionGenerator.bitmask( s ) );
	}
}
