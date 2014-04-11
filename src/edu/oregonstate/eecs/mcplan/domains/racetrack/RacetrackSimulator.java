/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racetrack;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;

/**
 * @author jhostetler
 *
 */
public class RacetrackSimulator implements UndoSimulator<RacetrackState, RacetrackAction>
{
	private class StepAction extends RacetrackAction
	{
		private double old_x_ = 0;
		private double old_y_ = 0;
		private double old_dx_ = 0;
		private double old_dy_ = 0;
		private boolean old_crashed_ = false;
		private boolean old_on_track_ = true;
		private double old_theta_ = 0;
		
		private boolean done_ = false;
		
		@Override
		public void undoAction( final RacetrackState s )
		{
			s.car_x = old_x_;
			s.car_y = old_y_;
			s.car_dx = old_dx_;
			s.car_dy = old_dy_;
			s.crashed = old_crashed_;
			s.on_track = old_on_track_;
			s.car_theta = old_theta_;
			done_ = false;
		}

		@Override
		public void doAction( final RacetrackState s )
		{
//			System.out.println( "*** Step" );
			
			// Store state
			old_x_ = s.car_x;
			old_y_ = s.car_y;
			old_dx_ = s.car_dx;
			old_dy_ = s.car_dy;
			old_crashed_ = s.crashed;
			old_on_track_ = s.on_track;
			old_theta_ = s.car_theta;
			
			// Compute noisy control signal
			final double control_noise_x = control_noise_.sample();
			final double control_noise_y = control_noise_.sample();
			final double car_accel_x = (s.car_ddx + ((s.car_ddx + 1) * control_noise_x));
			final double car_accel_y = (s.car_ddy + ((s.car_ddy + 1) * control_noise_y));
			
			// Dynamics
			double t = 0.0;
			final double eps = 0.001;
			while( t < tstep_ - eps ) {
				// Current velocity
				final double vmag = Math.sqrt( s.car_dx*s.car_dx + s.car_dy*s.car_dy );
				
				// Standard aerodynamic drag
				final double Fdrag = 0.5 * frontal_area_ * air_density_ * drag_coefficient_ * vmag*vmag;
				// If car is off track, it gets slowed by e.g. gravel traps
				final double adrag = static_friction_accel_
								   + ((Fdrag / s.car_mass)
									 	* (s.on_track ? 1.0 : off_track_drag_multiplier_));
//				System.out.println( "adrag = " + adrag );

				// Largest possible displacement, to look for collisions
				final double rem_time = tstep_ - t;
				final double no_drag_dx = (s.car_dx + 0.5*car_accel_x*rem_time)*rem_time;
				final double no_drag_dy = (s.car_dy + 0.5*car_accel_y*rem_time)*rem_time;
				final double projected_x = s.car_x + no_drag_dx;
				final double projected_y = s.car_y + no_drag_dy;
				final Coordinate[] coordinates = new Coordinate[] {
					new Coordinate( s.car_x, s.car_y ),
					new Coordinate( projected_x, projected_y )
				};
				final LineString v = s.circuit.geom_factory.createLineString( coordinates );
				// Compute end of segment
				final Point cross = s.circuit.crossesTrackBoundary( v );
				final double seg_x;
				final double seg_y;
				if( cross == null ) {
					seg_x = projected_x;
					seg_y = projected_y;
				}
				else {
					seg_x = cross.getX() + eps*Math.cos( s.car_theta ); // + s.car_dx*0.03;
					seg_y = cross.getY() + eps*Math.sin( s.car_theta ); // + s.car_dy*0.03;
					
				}
				s.on_track = s.circuit.track.contains(
					s.circuit.geom_factory.createPoint( new Coordinate( seg_x, seg_y ) ) );
//				System.out.println( "Segment end: " + seg_x + " " + seg_y );
				
				// Compute transit time to end of segment
				final double dx = seg_x - s.car_x;
				final double dy = seg_y - s.car_y;
				final double dist = Math.sqrt( dx*dx + dy*dy );
				// x = vt + 1/2 a t^2 => 1/2 a t^2 + vt - x = 0
				//     => solve for t with quardratic formula
				final double transit_time = (-vmag + Math.sqrt( vmag*vmag + 2*adrag*dist )) / adrag;
				final double deltaT = transit_time < rem_time - eps
									  ? transit_time : rem_time;
//				System.out.println( "deltaT = " + deltaT );
				final double a_deltaV = 0.5*adrag*deltaT;
				final double control_deltaVx = 0.5*car_accel_x*deltaT;
				final double control_deltaVy = 0.5*car_accel_y*deltaT;
				final double uncontrolled_distance = -a_deltaV*deltaT + vmag*deltaT;
				final double new_x = s.car_x + control_deltaVx*deltaT + Math.cos( s.car_theta )*uncontrolled_distance;
				final double new_y = s.car_y + control_deltaVy*deltaT + Math.sin( s.car_theta )*uncontrolled_distance;
				
				coordinates[1].x = new_x;
				coordinates[1].y = new_y;
				final LineString vtrue = s.circuit.geom_factory.createLineString( coordinates );
				final Point impact = s.circuit.hitsWall( vtrue );
				if( impact != null ) {
//					System.out.println( "Crashed!" );
					s.car_x = impact.getX();
					s.car_y = impact.getY();
					s.car_dx = 0;
					s.car_dy = 0;
					s.crashed = true;
					break;
				}
				else {
					s.car_x = new_x;
					s.car_y = new_y;
					s.car_dx += control_deltaVx - Math.cos( s.car_theta )*a_deltaV;
					s.car_dy += control_deltaVy - Math.sin( s.car_theta )*a_deltaV;
					t += deltaT;
				}
			}
			
			// Current velocity
			final double vmag = Math.sqrt( s.car_dx*s.car_dx + s.car_dy*s.car_dy );
			if( vmag > eps ) {
				s.car_theta = Math.atan2( s.car_dy, s.car_dx );
			}
//			System.out.println( "state = " + s );
//			System.out.println( "vmag = " + vmag );
		
			final int next_sector = (s.sector + 1) % s.circuit.sectors.size();
			if( s.circuit.sectors.get( next_sector ).contains(
					s.circuit.geom_factory.createPoint( new Coordinate( s.car_x, s.car_y ) ) ) ) {
				if( s.sector == s.circuit.sectors.size() - 1 && next_sector == 0 ) {
					s.laps_to_go -= 1;
				}
				s.sector = next_sector;
			}
			
			done_ = true;
		}
		
		@Override
		public boolean isDone()
		{
			return done_;
		}

		@Override
		public RacetrackAction create()
		{
			return new StepAction();
		}
	}
	
	private final RacetrackState s_;
	
	private final Deque<JointAction<RacetrackAction>> action_history_
		= new ArrayDeque<JointAction<RacetrackAction>>();
	
	private final RandomGenerator rng_;
	private final RealDistribution control_noise_;
	
	private final double static_friction_accel_ = 0.5; // m/s^2 -- Represents mechanical friction
	private final double drag_coefficient_ = 2.0; // unitless -- This is higher than reality, but we need something
												  // to limit maximum speed.
	private final double air_density_ = 1.204; // kg/m^3 -- Wikipedia, 20 deg C
	private final double frontal_area_ = 2.0; // m^2
	private final double off_track_drag_multiplier_ = 30.0;
	
	private final double tstep_ = 0.2; // s -- Size of integration step
	
	public RacetrackSimulator( final RandomGenerator rng,
							   final RacetrackState s,
							   final double control_sigma )
	{
		rng_ = rng;
		s_ = s;
		control_noise_ = new NormalDistribution(
			rng_, 0, control_sigma, NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY );
	}
	
	@Override
	public RacetrackState state()
	{
		return s_;
	}

	@Override
	public void takeAction( final JointAction<RacetrackAction> a )
	{
		assert( a.nagents == 1 );
		// Control
		a.get( 0 ).doAction( s_ );
		action_history_.push( a );
		
		// Dynamics
		final StepAction dyn = new StepAction();
		dyn.doAction( s_ );
		action_history_.push( new JointAction<RacetrackAction>( dyn ) );
	}

	@Override
	public void untakeLastAction()
	{
		// Dynamics
		final JointAction<RacetrackAction> dyn = action_history_.pop();
		dyn.get( 0 ).undoAction( s_ );
		
		// Control
		final JointAction<RacetrackAction> a = action_history_.pop();
		a.get( 0 ).undoAction( s_ );
	}

	@Override
	public long depth()
	{
		// Because we're using dynamics actions, we divide by 2.
		return action_history_.size() / 2;
	}

	@Override
	public long t()
	{
		// Because we're using dynamics actions, we divide by 2.
		return action_history_.size() / 2;
	}

	@Override
	public int nagents()
	{
		return 1;
	}

	@Override
	public int[] turn()
	{
		return new int[] { 0 };
	}
	
	@Override
	public double[] reward()
	{
		if( s_.crashed ) {
			return new double[] { -10000 };
		}
		else if( s_.isTerminal() ) {
			return new double[] { 0 };
		}
		else {
			return new double[] { -1 };
		}
	}

	@Override
	public boolean isTerminalState()
	{
		return s_.isTerminal();
	}

	@Override
	public long horizon()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public String detailString()
	{
		return "RacetrackSimulator";
	}

}
