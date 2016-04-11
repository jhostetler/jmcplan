/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

/**
 * @author jhostetler
 *
 */
public class TripShuntSetAction extends CosmicAction
{
	public final int[] shunts;
	
	public TripShuntSetAction( final int... shunts )
	{
		this.shunts = shunts;
	}
	
	@Override
	public CosmicAction create()
	{
		return new TripShuntSetAction( shunts );
	}

	@Override
	public MWNumericArray toMatlab( final CosmicParameters params, final double t )
	{
		final MWNumericArray a = MWNumericArray.newInstance(
			new int[] { shunts.length, params.ev_cols }, MWClassID.DOUBLE, MWComplexity.REAL );
		for( int i = 0; i < shunts.length; ++i ) {
			a.set( new int[] { i+1, params.ev_time }, t );
			a.set( new int[] { i+1, params.ev_type }, params.ev_trip_shunt );
			a.set( new int[] { i+1, params.ev_shunt_loc }, shunts[i] );
		}
//		System.out.println( "TripShuntAction -> " + a );
		return a;
	}
	
	@Override
	public void applyNonCosmicChanges( final CosmicState sprime )
	{ }

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ Arrays.hashCode( shunts );
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof TripShuntSetAction) ) {
			return false;
		}
		final TripShuntSetAction that = (TripShuntSetAction) obj;
		return Arrays.equals( shunts, that.shunts );
	}
	
	@Override
	public String toString()
	{
		return "TripShuntSet(" + StringUtils.join( shunts, ';' ) + ")";
	}

}
