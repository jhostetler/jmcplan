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
	public double car_ddx = 0;
	public double car_ddy = 0;
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
	
	public RacetrackState( final Circuit circuit )
	{
		this.circuit = circuit;
		car_x = circuit.start.x;
		car_y = circuit.start.y;
		car_theta = circuit.orientation;
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
