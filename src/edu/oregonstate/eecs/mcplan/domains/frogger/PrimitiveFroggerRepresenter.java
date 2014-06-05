/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.frogger;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class PrimitiveFroggerRepresenter implements FactoredRepresenter<FroggerState, FactoredRepresentation<FroggerState>>
{
	private final ArrayList<Attribute> attributes_;

	public PrimitiveFroggerRepresenter( final FroggerParameters params )
	{
		attributes_ = new ArrayList<Attribute>();
		attributes_.add( new Attribute( "x" ) );
		attributes_.add( new Attribute( "y" ) );
		for( int i = 0; i < params.lanes; ++i ) {
			for( int j = 0; j < params.road_length; ++j ) {
				attributes_.add( new Attribute( "c" + (i+1) + "_" + j ) );
			}
		}
	}
	
	private PrimitiveFroggerRepresenter( final PrimitiveFroggerRepresenter that )
	{
		attributes_ = that.attributes_;
	}
	
	@Override
	public PrimitiveFroggerRepresenter create()
	{
		return new PrimitiveFroggerRepresenter( this );
	}

	@Override
	public PrimitiveFroggerRepresentation encode( final FroggerState s )
	{
		return new PrimitiveFroggerRepresentation( s );
	}
	
	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes_;
	}
	
	@Override
	public String toString()
	{
		return "PrimitiveFroggerRepresenter";
	}
}
