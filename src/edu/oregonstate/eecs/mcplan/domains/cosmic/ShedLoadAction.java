/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

/**
 * @deprecated Duplicate of TripShuntAction? Maybe once intended for fractional
 * load shedding.
 * @author jhostetler
 *
 */
@Deprecated
public class ShedLoadAction extends CosmicAction
{
	public final int shunt;
	
	public ShedLoadAction( final int shunt )
	{
		this.shunt = shunt;
	}
	
	@Override
	public CosmicAction create()
	{
		return new ShedLoadAction( shunt );
	}

	@Override
	public MWNumericArray toMatlab( final CosmicParameters params, final double t )
	{
		final MWNumericArray a = MWNumericArray.newInstance(
			new int[] { 1, params.ev_cols }, MWClassID.DOUBLE, MWComplexity.REAL );
		a.set( params.ev_time, t );
		a.set( params.ev_type, params.ev_trip_shunt );
		a.set( params.ev_shunt_loc, shunt );
//		System.out.println( "ShedLoadAction -> " + a );
		return a;
	}
	
	@Override
	public void applyNonCosmicChanges( final CosmicState sprime )
	{ }

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ (shunt + 1);
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof ShedLoadAction) ) {
			return false;
		}
		final ShedLoadAction that = (ShedLoadAction) obj;
		return shunt == that.shunt;
	}
	
	@Override
	public String toString()
	{
		return "ShedLoad(" + shunt + ")";
	}

}
