/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import gnu.trove.list.TIntList;

import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

/**
 * @author jhostetler
 *
 */
public class ShedZoneAction extends CosmicAction
{
	public final int zone;
	public final double amount;
	
	public ShedZoneAction( final int zone, final double amount )
	{
		this.zone = zone;
		this.amount = amount;
	}

	@Override
	public ShedZoneAction create()
	{
		return new ShedZoneAction( zone, amount );
	}

	@Override
	public MWNumericArray toMatlab( final CosmicParameters params, final double t )
	{
		final TIntList shunts = params.shuntsForZone( zone );
		final MWNumericArray a = MWNumericArray.newInstance(
			new int[] { shunts.size(), params.ev_cols }, MWClassID.DOUBLE, MWComplexity.REAL );
		for( int i = 0; i < shunts.size(); ++i ) {
			a.set( new int[] { i+1, params.ev_time }, t );
			a.set( new int[] { i+1, params.ev_type }, params.ev_shed_load );
			a.set( new int[] { i+1, params.ev_shunt_loc }, shunts.get( i ) );
			a.set( new int[] { i+1, params.ev_change_by }, CosmicParameters.ev_change_by_percent );
			a.set( new int[] { i+1, params.ev_quantity }, amount );
		}
//		System.out.println( "ShedLoadAction -> " + a );
		return a;
	}

	@Override
	public void applyNonCosmicChanges( final CosmicState sprime )
	{ }

	@Override
	public int hashCode()
	{
		return getClass().hashCode() + 3*(zone + 5*(Double.valueOf( amount ).hashCode()));
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof ShedZoneAction) ) {
			return false;
		}
		final ShedZoneAction that = (ShedZoneAction) obj;
		return zone == that.zone && amount == that.amount;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "ShedZone(" ).append( zone ).append( "; " ).append( amount ).append( ")" );
		return sb.toString();
	}
}
