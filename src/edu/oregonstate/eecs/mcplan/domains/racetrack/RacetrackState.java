/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racetrack;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import edu.oregonstate.eecs.mcplan.State;

/**
 * @author jhostetler
 *
 */
public class RacetrackState implements State
{
	public final Circuit circuit;
	
	public double car_x = 0;
	public double car_y = 0;
	public double car_dx = 0;
	public double car_dy = 0;
	public double car_accel_v = 0;
	public double car_accel_theta = 0;
	public boolean crashed = false;
	public boolean on_track = true;
	public int sector = -1;
	public int laps_to_go = 1;
	
	public double car_theta = 0;
	
	// Car parameters are round numbers similar to F1 car performance
	public final double car_mass = 800; // kg
	public final double adhesion_limit = 4 * 9.8; // m/s^2 -- 4g maximum acceleration
	public final double car_width = 2.0; // m
	public final double car_length = 4.0; // m
	
	public final double static_friction_accel_ = 0.5; // m/s^2 -- Represents mechanical friction
	public final double drag_coefficient_ = 2.0; // unitless -- This is higher than reality, but we need something
												  // to limit maximum speed.
	public final double air_density_ = 1.204; // kg/m^3 -- Wikipedia, 20 deg C
	public final double frontal_area_ = 2.0; // m^2
	public final double off_track_drag_multiplier_ = 30.0;
	
	public RacetrackState( final Circuit circuit )
	{
		this.circuit = circuit;
		car_x = circuit.start.x;
		car_y = circuit.start.y;
		car_theta = circuit.orientation;
	}
	
	public double terminal_velocity()
	{
		return Math.sqrt( 2*car_mass*adhesion_limit / air_density_*frontal_area_*drag_coefficient_ );
	}
	
	public RealVector velocity()
	{
		return new ArrayRealVector( new double[] { car_dx, car_dy } );
	}
	
	@Override
	public String toString()
	{
		return "[" + car_x + ", " + car_y + ", " + car_dx + ", " + car_dy + "]";
	}

	@Override
	public boolean isTerminal()
	{
		return crashed || laps_to_go == 0;
	}
}
