/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.advising;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ArrayFactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class AdvisingGroundRepresenter implements FactoredRepresenter<AdvisingState, FactoredRepresentation<AdvisingState>>
{
	private final ArrayList<Attribute> attributes;
	
	public AdvisingGroundRepresenter( final AdvisingParameters params )
	{
		attributes = new ArrayList<Attribute>();
		for( int i = 0; i < params.Ncourses; ++i ) {
			attributes.add( new Attribute( "grade" + i ) );
		}
	}
	
	private AdvisingGroundRepresenter( final AdvisingGroundRepresenter that )
	{
		this.attributes = that.attributes;
	}
	
	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}
	
	@Override
	public AdvisingGroundRepresenter create()
	{
		return new AdvisingGroundRepresenter( this );
	}

	@Override
	public FactoredRepresentation<AdvisingState> encode( final AdvisingState s )
	{
		final float[] phi = Fn.copyAsFloat( s.grade );
		return new ArrayFactoredRepresentation<AdvisingState>( phi );
	}
}
