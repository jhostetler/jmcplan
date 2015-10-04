/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.sailing;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ArrayFactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class SailingGroundRepresenter implements FactoredRepresenter<SailingState, FactoredRepresentation<SailingState>>
{
	private static ArrayList<Attribute> attributes;
	static {
		attributes = new ArrayList<Attribute>();
		attributes.add( new Attribute( "x" ) );
		attributes.add( new Attribute( "y" ) );
		attributes.add( new Attribute( "w" ) );
		attributes.add( new Attribute( "v" ) );
	}
	
	@Override
	public FactoredRepresenter<SailingState, FactoredRepresentation<SailingState>> create()
	{
		return new SailingGroundRepresenter();
	}

	@Override
	public FactoredRepresentation<SailingState> encode( final SailingState s )
	{
		final float[] x = new float[4];
		x[0] = s.x;
		x[1] = s.y;
		x[2] = s.w;
		x[3] = s.v;
		return new ArrayFactoredRepresentation<SailingState>( x );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}
}
