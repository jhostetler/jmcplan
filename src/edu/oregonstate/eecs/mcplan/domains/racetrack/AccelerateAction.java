/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racetrack;

import org.apache.commons.math3.random.RandomGenerator;


/**
 * @author jhostetler
 *
 */
public class AccelerateAction extends RacetrackAction
{
	private boolean done_ = false;
	
	public final double v;
	public final double theta;
	
//	public static AccelerateAction fromPolar( final double theta, final double r )
//	{
//		return new AccelerateAction( r*Math.cos( theta ), r*Math.sin( theta ) );
//	}
	
	public AccelerateAction( final double v, final double theta )
	{
		this.v = v;
		this.theta = theta;
	}
	
	@Override
	public void undoAction( final RacetrackState s )
	{
		assert( done_ );
		
		s.car_accel_v = 0;
		s.car_accel_theta = 0;
		
		done_ = false;
	}

	@Override
	public void doAction( final RandomGenerator rng, final RacetrackState s )
	{
		assert( !done_ );

		s.car_accel_v = v;
		s.car_accel_theta = theta;
		
		done_ = true;
	}

	@Override
	public boolean isDone()
	{
		return done_;
	}

	@Override
	public AccelerateAction create()
	{
		return new AccelerateAction( v, theta );
	}
	
	@Override
	public int hashCode()
	{
		final long bits = (41 + 37 * Double.doubleToLongBits( v )) * 43 + Double.doubleToLongBits( theta );
		return (int) (bits ^ (bits >>> 32));
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof AccelerateAction) ) {
			return false;
		}
		final AccelerateAction that = (AccelerateAction) obj;
		return v == that.v && theta == that.theta;
	}
	
	@Override
	public String toString()
	{
		return "Accelerate[" + v + ", " + theta + "]";
	}

}
