/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class TripShuntAction extends CosmicAction
{
	public final int shunt;
	
	public TripShuntAction( final int shunt )
	{
		this.shunt = shunt;
	}
	
	@Override
	public CosmicAction create()
	{
		return new TripShuntAction( shunt );
	}

	@Override
	public MWNumericArray toMatlab( final CosmicParameters params, final double t )
	{
		final MWNumericArray a = MWNumericArray.newInstance(
			new int[] { 1, params.ev_cols }, Fn.repeat( 0.0, params.ev_cols ), MWClassID.DOUBLE );
		a.set( params.ev_time, t );
		a.set( params.ev_type, params.ev_trip_shunt );
		a.set( params.ev_shunt_loc, shunt );
		System.out.println( "TripShuntAction -> " + a );
		return a;
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ (shunt + 1);
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof TripShuntAction) ) {
			return false;
		}
		final TripShuntAction that = (TripShuntAction) obj;
		return shunt == that.shunt;
	}
	
	@Override
	public String toString()
	{
		return "TripShunt(" + shunt + ")";
	}

}
