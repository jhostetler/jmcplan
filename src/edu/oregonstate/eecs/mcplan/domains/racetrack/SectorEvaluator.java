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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import edu.oregonstate.eecs.mcplan.search.EvaluationFunction;
import edu.oregonstate.eecs.mcplan.sim.Simulator;
import edu.oregonstate.eecs.mcplan.util.Tuple.Tuple2;

/**
 * @author jhostetler
 *
 */
public class SectorEvaluator implements EvaluationFunction<RacetrackState, RacetrackAction>
{
	public final double max_speed;
	public final double tstep = RacetrackSimulator.tstep_;
	
	public SectorEvaluator( final double max_speed )
	{
		this.max_speed = max_speed;
	}
	
	@Override
	public double[] evaluate( final Simulator<RacetrackState, RacetrackAction> sim )
	{
		final RacetrackState s = sim.state();
		final double x_proj = s.car_x + tstep*s.car_dx;
		final double y_proj = s.car_y + tstep*s.car_dy;
		int segment = s.circuit.segment( s.car_x, s.car_y );
//		System.out.println( "segment = " + segment );
		Point p = s.circuit.geom_factory.createPoint( new Coordinate( s.car_x, s.car_y ) );
		double d = 0.0;
		if( segment < 0 ) {
			// Off track
//			System.out.println( "Off track!" );
			final Tuple2<Integer, Point> t = s.circuit.nearestTrackPoint( s.car_x, s.car_y );
			segment = t._1;
//			System.out.println( "Nearest segment = " + segment );
			d += p.distance( t._2 );
			p = t._2;
		}

		// There are some complications with crossing the finish line that we
		// handle with special cases.
		//
		// TODO: Possibly a better way is to remove the special cases but
		// correct the laps_to_go to account for crossing the finish. This
		// would result in *adding* distance on the last lap to compensate
		// the error introduced by handling laps uniformly. It avoids having
		// to calculate distance to a different point in the last segment.
		if( segment == s.circuit.segments().size() - 1 ) {
			// In the last segment, measure to the *current* reference point
			// rather than the next one, in case the race is going to end.
			// This should be OK, since the finish line will tend to be in
			// a straight section.
			final int next = segment;
			final Point next_point = s.circuit.reference_points().get( next );
			d += p.distance( next_point );
			if( s.sector != s.circuit.sectors.size() - 1 ) {
				d += s.circuit.circuit_length[next];
//				System.out.println( "Bad sector - next = " + next );
			}
		}
		else {
			final int next = (segment + 1) % s.circuit.segments().size();
			final Point next_point = s.circuit.reference_points().get( next );
			d += p.distance( next_point );
			// Don't add total distance around the entire track, since that
			// will be added by the "laps to go" addition below.
//			System.out.println( s.sector );
			if( s.sector != s.circuit.sectors.size() - 1 ) {
				d += s.circuit.circuit_length[next];
//				System.out.println( "Bad sector - next = " + next );
			}
			else if( next != s.circuit.segments().size() - 1 ) {
				d += s.circuit.circuit_length[next];
			}
		}
		
		d += (s.laps_to_go - 1) * s.circuit.circuit_length[s.circuit.circuit_length.length - 1];
		
		return new double[] { -d / (max_speed * tstep) };
	}

}
