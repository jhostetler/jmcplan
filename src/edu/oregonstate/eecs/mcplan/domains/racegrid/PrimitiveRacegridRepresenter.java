/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racegrid;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class PrimitiveRacegridRepresenter implements FactoredRepresenter<RacegridState, FactoredRepresentation<RacegridState>>
{
	private final ArrayList<Attribute> attributes_;

	public PrimitiveRacegridRepresenter()
	{
		attributes_ = new ArrayList<Attribute>();
		attributes_.add( new Attribute( "x" ) );
		attributes_.add( new Attribute( "y" ) );
		attributes_.add( new Attribute( "dx" ) );
		attributes_.add( new Attribute( "dy" ) );
	}
	
	private PrimitiveRacegridRepresenter( final PrimitiveRacegridRepresenter that )
	{
		attributes_ = that.attributes_;
	}
	
	@Override
	public PrimitiveRacegridRepresenter create()
	{
		return new PrimitiveRacegridRepresenter( this );
	}

	@Override
	public PrimitiveRacegridRepresentation encode( final RacegridState s )
	{
		return new PrimitiveRacegridRepresentation( s );
	}
	
	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes_;
	}
	
	@Override
	public String toString()
	{
		return "PrimitiveRacegridRepresenter";
	}
}
