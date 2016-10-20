/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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

	@Override
	public void close()
	{ }
}
