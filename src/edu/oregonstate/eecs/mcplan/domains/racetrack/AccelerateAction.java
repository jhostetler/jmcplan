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
