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
public class TripBusAction extends CosmicAction
{
	private final int bus;
	
	public TripBusAction( final int bus )
	{
		this.bus = bus;
	}
	
	@Override
	public CosmicAction create()
	{
		return new TripBusAction( bus );
	}

	@Override
	public MWNumericArray toMatlab( final CosmicParameters params, final double t )
	{
		final MWNumericArray a = MWNumericArray.newInstance(
			new int[] { 1, params.ev_cols }, Fn.repeat( 0.0, params.ev_cols ), MWClassID.DOUBLE );
		a.set( params.ev_time, t );
		a.set( params.ev_type, params.ev_trip_bus );
		a.set( params.ev_bus_loc, bus );
		System.out.println( "TripBusAction -> " + a );
		return a;
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ (bus + 1);
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof TripBusAction) ) {
			return false;
		}
		final TripBusAction that = (TripBusAction) obj;
		return bus == that.bus;
	}
	
	@Override
	public String toString()
	{
		return "TripBus(" + bus + ")";
	}
}
