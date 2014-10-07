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
	private final int vision_;

	public RelativeFroggerRepresenter( final FroggerParameters params, final int vision )
	{
		vision_ = vision;
		attributes_ = new ArrayList<Attribute>();
		attributes_.add( new Attribute( "x" ) );
		attributes_.add( new Attribute( "y" ) );
		for( int i = vision; i >= -vision; --i ) {
			for( int j = -vision; j <= vision; ++j ) {
				if( i == 0 && j == 0 ) {
					continue;
				}
				attributes_.add( new Attribute(
					"car_x" + (j >= 0 ? "+" : "") + j + "_y" + (i >= 0 ? "+" : "") + i ) );
			}
		}
	}
	
	private RelativeFroggerRepresenter( final RelativeFroggerRepresenter that )
	{
		attributes_ = that.attributes_;
		vision_ = that.vision_;
	}
	
	@Override
	public RelativeFroggerRepresenter create()
	{
		return new RelativeFroggerRepresenter( this );
	}

	@Override
	public RelativeFroggerRepresentation encode( final FroggerState s )
	{
		return new RelativeFroggerRepresentation( s, vision_ );
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
