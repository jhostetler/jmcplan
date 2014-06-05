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
public class RelativeFroggerRepresenter implements FactoredRepresenter<FroggerState, FactoredRepresentation<FroggerState>>
{
	private final ArrayList<Attribute> attributes_;

	public RelativeFroggerRepresenter( final FroggerParameters params )
	{
		attributes_ = new ArrayList<Attribute>();
		attributes_.add( new Attribute( "x" ) );
		attributes_.add( new Attribute( "y" ) );
		final int road_vision = params.road_length - 1;
		for( int i = params.lanes; i >= -params.lanes + 1; --i ) {
			for( int j = -road_vision; j <= road_vision; ++j ) {
				attributes_.add( new Attribute(
					"car_x" + (j >= 0 ? "+" : "") + j + "_y" + (i >= 0 ? "+" : "") + i ) );
			}
		}
	}
	
	private RelativeFroggerRepresenter( final RelativeFroggerRepresenter that )
	{
		attributes_ = that.attributes_;
	}
	
	@Override
	public RelativeFroggerRepresenter create()
	{
		return new RelativeFroggerRepresenter( this );
	}

	@Override
	public RelativeFroggerRepresentation encode( final FroggerState s )
	{
		return new RelativeFroggerRepresentation( s );
	}
	
	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes_;
	}
	
	@Override
	public String toString()
	{
		return "RelativeFroggerRepresenter";
	}
}
