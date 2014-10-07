/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.fuelworld;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class PrimitiveFuelWorldRepresenter
	implements FactoredRepresenter<FuelWorldState, FactoredRepresentation<FuelWorldState>>
{
	private static final ArrayList<Attribute> attributes_ = new ArrayList<Attribute>();
	
	static {
		attributes_.add( new Attribute( "location" ) );
		attributes_.add( new Attribute( "fuel" ) );
		attributes_.add( new Attribute( "fuel_depot" ) );
	}

	@Override
	public PrimitiveFuelWorldRepresenter create()
	{
		return new PrimitiveFuelWorldRepresenter();
	}

	@Override
	public PrimitiveFuelWorldRepresentation encode( final FuelWorldState s )
	{
		return new PrimitiveFuelWorldRepresentation( s );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes_;
	}
}
