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
public class TripBranchSetAction extends CosmicAction
{
	private final int[] branch;
	
	public TripBranchSetAction( final int[] branch )
	{
		this.branch = branch;
	}
	
	@Override
	public CosmicAction create()
	{
		return new TripBranchSetAction( branch );
	}

	@Override
	public MWNumericArray toMatlab( final CosmicParameters params, final double t )
	{
		final MWNumericArray a = MWNumericArray.newInstance(
			new int[] { branch.length, params.ev_cols }, MWClassID.DOUBLE, MWComplexity.REAL );
		for( int i = 0; i < branch.length; ++i ) {
			a.set( new int[] { i+1, params.ev_time }, t );
			a.set( new int[] { i+1, params.ev_type }, params.ev_trip_branch );
			a.set( new int[] { i+1, params.ev_branch_loc }, branch[i] );
		}
//		System.out.println( "TripBranchSetAction -> " + a );
		return a;
	}
	
	@Override
	public void applyNonCosmicChanges( final CosmicState sprime )
	{ }

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ Arrays.hashCode( branch );
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof TripBranchSetAction) ) {
			return false;
		}
		final TripBranchSetAction that = (TripBranchSetAction) obj;
		return Arrays.equals( branch, that.branch );
	}
	
	@Override
	public String toString()
	{
		return "TripBranchSet[" + Arrays.toString( branch ) + "]";
	}
}
