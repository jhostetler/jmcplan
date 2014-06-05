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
