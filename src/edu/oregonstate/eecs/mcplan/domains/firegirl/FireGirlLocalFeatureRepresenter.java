/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.firegirl;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ArrayFactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * The feature representation used in the FireGirl Python code, but with
 * the bias feature and 'd2' removed, since we're not doing regression.
 */
public class FireGirlLocalFeatureRepresenter implements FactoredRepresenter<FireGirlState, FactoredRepresentation<FireGirlState>>
{
	private static final ArrayList<Attribute> attributes;
	
	static {
		attributes = new ArrayList<Attribute>();
		attributes.add( new Attribute( "ignite_date" ) );
		attributes.add( new Attribute( "ignite_temp" ) );
		attributes.add( new Attribute( "ignite_wind" ) );
		attributes.add( new Attribute( "timber_val" ) );
		attributes.add( new Attribute( "timber_ave8" ) );
		attributes.add( new Attribute( "timber_ave24" ) );
		attributes.add( new Attribute( "fuel" ) );
		attributes.add( new Attribute( "fuel_ave8" ) );
		attributes.add( new Attribute( "fuel_ave24" ) );
	}
	
	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}

	@Override
	public FactoredRepresenter<FireGirlState, FactoredRepresentation<FireGirlState>> create()
	{
		return new FireGirlLocalFeatureRepresenter();
	}

	@Override
	public FactoredRepresentation<FireGirlState> encode( final FireGirlState s )
	{
		final int[] loc = s.ignite_loc;
		final int x = loc[0];
		final int y = loc[1];
		
		final float[] phi = new float[attributes.size()];
		int idx = 0;
		phi[idx++] = s.ignite_date;
		phi[idx++] = s.ignite_temp;
		phi[idx++] = s.ignite_wind;
		phi[idx++] = (float) s.getPresentTimberValue( x, y );
		phi[idx++] = (float) s.getValueAverage( x, y, 1 ); //timber_ave8;
		phi[idx++] = (float) s.getValueAverage( x, y, 2 ); //timber_ave24;
		phi[idx++] = s.fuel_load[x][y];
		phi[idx++] = (float) s.getFuelAverage( x, y, 1 ); //fire_ave8;
		phi[idx++] = (float) s.getFuelAverage( x, y, 2 ); //fire_ave24;
		
		return new ArrayFactoredRepresentation<FireGirlState>( phi );
	}
}
