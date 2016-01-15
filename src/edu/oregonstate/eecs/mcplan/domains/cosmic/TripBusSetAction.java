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
public class TripBusSetAction extends CosmicAction
{
	private final int[] bus;
	
	public TripBusSetAction( final int[] bus )
	{
		this.bus = bus;
	}
	
	@Override
	public CosmicAction create()
	{
		return new TripBusSetAction( bus );
	}

	@Override
	public MWNumericArray toMatlab( final CosmicParameters params, final double t )
	{
		final MWNumericArray a = MWNumericArray.newInstance(
			new int[] { bus.length, params.ev_cols }, MWClassID.DOUBLE, MWComplexity.REAL );
		for( int i = 0; i < bus.length; ++i ) {
			a.set( new int[] { i+1, params.ev_time }, t );
			a.set( new int[] { i+1, params.ev_type }, params.ev_trip_bus );
			a.set( new int[] { i+1, params.ev_bus_loc }, bus[i] );
		}
//		System.out.println( "TripBusSetAction -> " + a );
		return a;
	}
	
	@Override
	public void applyNonCosmicChanges( final CosmicState sprime )
	{ }

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ Arrays.hashCode( bus );
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof TripBusSetAction) ) {
			return false;
		}
		final TripBusSetAction that = (TripBusSetAction) obj;
		return Arrays.equals( bus, that.bus );
	}
	
	@Override
	public String toString()
	{
		return "TripBusSet[" + Arrays.toString( bus ) + "]";
	}
}
