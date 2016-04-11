/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import java.util.Arrays;

import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

/**
 * @author jhostetler
 *
 */
public final class IslandAction extends CosmicAction
{
	private final int zone;
	private final int[] cutset;
	
	public IslandAction( final int zone, final int[] cutset )
	{
		this.zone = zone;
		this.cutset = cutset;
	}
	
	@Override
	public CosmicAction create()
	{
		return new IslandAction( zone, cutset );
	}

	@Override
	public MWNumericArray toMatlab( final CosmicParameters params, final double t )
	{
		final MWNumericArray m = MWNumericArray.newInstance(
			new int[] { cutset.length, params.ev_cols }, MWClassID.DOUBLE, MWComplexity.REAL );
		int row = 1;
		for( final int branch_id : cutset ) {
			m.set( new int[] { row, params.ev_time }, t );
			m.set( new int[] { row, params.ev_type }, params.ev_trip_branch );
			m.set( new int[] { row, params.ev_branch_loc }, branch_id );
			row += 1;
		}
//		System.out.println( toString() + " -> " + m );
		return m;
	}
	
	@Override
	public void applyNonCosmicChanges( final CosmicState sprime )
	{
		sprime.islands.add( zone );
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ Arrays.hashCode( cutset );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof IslandAction) ) {
			return false;
		}
		final IslandAction that = (IslandAction) obj;
		return zone == that.zone && Arrays.equals( cutset, that.cutset );
	}

	@Override
	public String toString()
	{
		return "Island(" + zone + ")";
	}

}
