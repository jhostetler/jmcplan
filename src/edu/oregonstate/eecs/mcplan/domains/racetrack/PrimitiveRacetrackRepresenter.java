/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racetrack;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class PrimitiveRacetrackRepresenter
	implements FactoredRepresenter<RacetrackState, FactoredRepresentation<RacetrackState>>
{
	private static final ArrayList<Attribute> attributes_ = new ArrayList<Attribute>();
	private static final int Nattributes_ = 2 + 2;
	static {
		attributes_.add( new Attribute( "x" ) );
		attributes_.add( new Attribute( "y" ) );
		attributes_.add( new Attribute( "dx" ) );
		attributes_.add( new Attribute( "dy" ) );
	}
	
	@Override
	public PrimitiveRacetrackRepresenter create()
	{
		return new PrimitiveRacetrackRepresenter();
	}

	@Override
	public PrimitiveRacetrackState encode( final RacetrackState s )
	{
		return new PrimitiveRacetrackState( s );
	}
	
	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes_;
	}
}
