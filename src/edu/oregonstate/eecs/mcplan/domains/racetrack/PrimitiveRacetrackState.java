/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racetrack;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;

/**
 * @author jhostetler
 *
 */
public class PrimitiveRacetrackState extends FactoredRepresentation<RacetrackState>
{
	private final double[] phi_;
	
	public PrimitiveRacetrackState( final RacetrackState s )
	{
		phi_ = new double[2 + 2];
		int idx = 0;
		phi_[idx++] = s.car_x;
		phi_[idx++] = s.car_y;
		final double speed = Math.sqrt( s.car_dx*s.car_dx + s.car_dy*s.car_dy );
		phi_[idx++] = s.car_theta;
		phi_[idx++] = speed;
		assert( idx == phi_.length );
	}
	
	private PrimitiveRacetrackState( final double[] phi )
	{
		phi_ = phi;
	}
	
	@Override
	public double[] phi()
	{
		return phi_;
	}

	@Override
	public PrimitiveRacetrackState copy()
	{
		return new PrimitiveRacetrackState( phi_ );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof PrimitiveRacetrackState) ) {
			return false;
		}
		final PrimitiveRacetrackState that = (PrimitiveRacetrackState) obj;
		return Arrays.equals( phi_, that.phi_ );
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( phi_ );
	}
}