/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racegrid;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;

/**
 * @author jhostetler
 *
 */
public class RacegridSimulator implements UndoSimulator<RacegridState, RacegridAction>
{
	private class StepAction extends RacegridAction
	{
		private int old_x_ = 0;
		private int old_y_ = 0;
		private int old_dx_ = 0;
		private int old_dy_ = 0;
		private int old_ddx_ = 0;
		private int old_ddy_ = 0;
		private boolean old_crashed_ = false;
		private boolean old_goal_ = false;
		private int old_t_ = 0;
		
		private boolean done_ = false;
		
		@Override
		public void undoAction( final RacegridState s )
		{
			assert( done_ );
			s.x = old_x_;
			s.y = old_y_;
			s.dx = old_dx_;
			s.dy = old_dy_;
			s.ddx = old_ddx_;
			s.ddy = old_ddy_;
			s.crashed = old_crashed_;
			s.goal = old_goal_;
			s.t = old_t_;
			done_ = false;
		}

		@Override
		public void doAction( final RacegridState s )
		{
			assert( !done_ );
//			System.out.println( "*** Step" );
			
			// Store state
			old_x_ = s.x;
			old_y_ = s.y;
			old_dx_ = s.dx;
			old_dy_ = s.dy;
			old_ddx_ = s.ddx;
			old_ddy_ = s.ddy;
			old_crashed_ = s.crashed;
			old_goal_ = s.goal;
			old_t_ = s.t;

			applyDynamics( rng_, s, slip_ );
			
			done_ = true;
		}
		
		@Override
		public boolean isDone()
		{
			return done_;
		}

		@Override
		public RacegridAction create()
		{
			return new StepAction();
		}
	}
	
	private static void applyBigNoise( final RandomGenerator rng, final RacegridState s, final double slip )
	{
		final int noisy_ddx;
		final double ddx_sample = rng.nextDouble();
		if( ddx_sample < slip ) {
			noisy_ddx = s.ddx - 1;
		}
		else if( ddx_sample > (1.0 - slip) ) {
			noisy_ddx = s.ddx + 1;
		}
		else {
			noisy_ddx = s.ddx;
		}
		
		final int noisy_ddy;
		final double ddy_sample = rng.nextDouble();
		if( ddy_sample < slip ) {
			noisy_ddy = s.ddy - 1;
		}
		else if( ddy_sample > (1.0 - slip) ) {
			noisy_ddy = s.ddy + 1;
		}
		else {
			noisy_ddy = s.ddy;
		}
		
		s.dx += noisy_ddx;
		s.dy += noisy_ddy;
	}
	
	private static void applySmallNoise( final RandomGenerator rng, final RacegridState s, final double slip )
	{
		final double r = rng.nextDouble();
		if( r < slip ) {
			// Don't update dx/dy
		}
		else {
			s.dx += s.ddx;
			s.dy += s.ddy;
		}
	}
	
	/**
	 * Applies noise independently to both directions. Noise is of the
	 * "either desired action or nothing" variety.
	 * @param rng
	 * @param s
	 * @param slip
	 */
	private static void applyFactoredSmallNoise( final RandomGenerator rng, final RacegridState s, final double slip )
	{
		final double rx = rng.nextDouble();
		if( rx < slip ) {
			// Don't update
		}
		else {
			s.dx += s.ddx;
		}
		
		final double ry = rng.nextDouble();
		if( ry < slip ) {
			// Don't update
		}
		else {
			s.dy += s.ddy;
		}
	}
	
	/**
	 * Note: This function advances 's.t'.
	 * @param rng
	 * @param s
	 * @param slip
	 */
	public static void applyDynamics( final RandomGenerator rng, final RacegridState s, final double slip )
	{
		if( slip > 0 ) {
//			applyBigNoise( rng, s, slip );
//			applySmallNoise( rng, s, slip );
			applyFactoredSmallNoise( rng, s, slip );
		}
		else {
			s.dx += s.ddx;
			s.dy += s.ddy;
		}
		
		s.ddx = 0;
		s.ddy = 0;
		
		final int proj_x = s.x + s.dx;
		final int proj_y = s.y + s.dy;
		
//		final Iterable<Point> line_cover = lineCover( s.x, s.y, proj_x, proj_y );
		final Iterable<Point> line_cover = rasterLineBresenham( s.x, s.y, proj_x, proj_y );
		
		Point prev = null;
		for( final Point p : line_cover ) {
//				System.out.println( "(" + p.x + ", " + p.y + ")" );
			if( p.x < 0 || p.x >= s.width || p.y < 0 || p.y >= s.height ) {
        		s.crashed = true;
				s.dx = 0;
				s.dy = 0;
				if( prev != null ) {
					s.x = prev.x;
					s.y = prev.y;
				}
				break;
        	}
			final TerrainType terrain = s.terrain[p.y][p.x];
//				System.out.println( terrain );
			if( terrain == TerrainType.Wall ) {
				s.crashed = true;
				s.dx = 0;
				s.dy = 0;
				if( prev != null ) {
					s.x = prev.x;
					s.y = prev.y;
				}
				break;
			}
			else if( terrain == TerrainType.Goal ) {
				s.goal = true;
				s.x = p.x;
				s.y = p.y;
				break;
			}
			
			prev = p;
		}
		
		if( !s.goal && !s.crashed ) {
			s.x = proj_x;
			s.y = proj_y;
		}
		
		s.t += 1;
	}
	
	private static final class Point
	{
		public final int x;
		public final int y;
		
		public Point( final int x, final int y )
		{
			this.x = x;
			this.y = y;
		}
		
		@Override
		public String toString()
		{ return "(" + x + ", " + y + ")"; }
	}
	
	/**
	 * Bresenham's algorithm for rasterizing a line.
	 * <p>
	 * Note that this algorithm does *not* return every cell that the line
	 * would pass through. That is what lineCover() attempts to do.
	 * <p>
	 * Adapted from:
	 * http://tomasjanecek.cz/en/clanky/post/trivial-dda-and-bresenham-algorithm-for-a-line-in-java
	 * <p>
	 * Note that the linked implementation does not properly add the starting
	 * cell if the line consists of more than one pixel.
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	private static Iterable<Point> rasterLineBresenham( int x1, int y1, final int x2, final int y2 )
	{
		final ArrayList<Point> line = new ArrayList<Point>();
		line.add( new Point( x1, y1 ) );
		if( !(x1 == x2 && y1 == y2) ) {
			final int dx = Math.abs( x2 - x1 );
			final int dy = Math.abs( y2 - y1 );
			int diff = dx - dy;
			
			final int shift_x = (x1 < x2 ? 1 : -1);
			final int shift_y = (y1 < y2 ? 1 : -1);
			do { // We know the condition is satisfied the first time around
				final int p = 2 * diff;
				if( p > -dy ) {
					diff -= dy;
					x1 += shift_x;
				}
				if( p < dx ) {
					diff += dx;
					y1 += shift_y;
				}
				line.add( new Point( x1, y1 ) );
			} while( !(x1 == x2 && y1 == y2) );
		}
		return line;
	}
	
	/**
	 * Computes all grid cells that a line intersects.
	 * 
	 * Based on an implementation of the DDA algorithm from:
	 * http://tomasjanecek.cz/en/clanky/post/trivial-dda-and-bresenham-algorithm-for-a-line-in-java
	 * 
	 * Note that the above implementation doesn't handle rounding errors well
	 * and doesn't return the grid cells in traversal order.
	 * 
	 * @param ix1
	 * @param iy1
	 * @param ix2
	 * @param iy2
	 * @return
	 */
	private static Iterable<Point> lineCover( final int ix1, final int iy1, final int ix2, final int iy2 )
	{
		final ArrayList<Point> points = new ArrayList<Point>();
		if( ix1 == ix2 && iy1 == iy2 ) {
			points.add( new Point( ix2, iy2 ) );
			return points;
		}
		
		// The grid coordinates actually represent the middle of a grid cell
		final int x1 = 2*ix1 + 1;
		final int y1 = 2*iy1 + 1;
		final int x2 = 2*ix2 + 1;
		final int y2 = 2*iy2 + 1;
		
		final double dx = x2-x1;
		final double dy = y2-y1;

		points.add( new Point( ix1, iy1 ) );
		
		if (Math.abs(y2 - y1) <= Math.abs(x2 - x1)) { // |m| <= 1
			// Because of the slope constraint, the y coordinate cannot change
			// in only half of a grid cell in the x direction. The idea of
			// the algorithm is:
			// 1. Do the second half of the origin cell.
			// 2. Do the entire width of middle cells. See if y changes. If it
			//    does, add both top and bottom cells in that x coordinate
			// 3. Do the first half of the destionation cell
			
			// FIXME: You need to reverse the sign of 'm' also. See the
			// implementation from the ROB599 HW1.
			
			// Reverse x traversal if x is decreasing
			final int stride = (x2 < x1 ? -1 : 1);
			final double m = dy/dx;
			double y = y1;
			
			// We've already added the origin point
			int x = x1;
			x += stride;
			y += m;
			
			while( x + stride != x2 ) {
				final int old_floor_y = (int) Math.floor( y / 2.0 );
				y += 2*m;
				final int new_floor_y = (int) Math.floor( y / 2.0 );
				x += stride;
				points.add( new Point( (x-1)/2, old_floor_y ) );
				if( old_floor_y != new_floor_y ) {
					// We increased the y coordinate in this x coordinate
					points.add( new Point( (x-1)/2, new_floor_y ) );
				}
				x += stride;
			}
			
			// Last point will be added below
		}
		else { // |m| > 1
			final int stride = (y2 < y1 ? -1 : 1);
			final double m = dx/dy;
			double x = x1;
			
			// We've already added the origin point
			int y = y1;
			y += stride;
			x += m;
			
			while( y + stride != y2 ) {
				final int old_floor_x = (int) Math.floor( x / 2.0 );
				x += 2*m;
				final int new_floor_x = (int) Math.floor( x / 2.0 );
				y += stride;
				points.add( new Point( old_floor_x, (y-1)/2 ) );
				if( old_floor_x != new_floor_x ) {
					// We increased the x coordinate in this y coordinate
					points.add( new Point( new_floor_x, (y-1)/2 ) );
				}
				y += stride;
			}
			
			// Last point will be added below
		}
		
		points.add( new Point( ix2, iy2 ) );
		
		return points;
	}
	
	// -----------------------------------------------------------------------
	
	private final RandomGenerator rng_;
	private final RacegridState s_;
	private final double slip_;
	
	private final Deque<RacegridAction> action_history_ = new ArrayDeque<RacegridAction>();
	private final int Nevents_ = 2;
	
	public RacegridSimulator( final RandomGenerator rng, final RacegridState s, final double slip )
	{
		rng_ = rng;
		s_ = s;
		slip_ = slip;
	}
	
	@Override
	public RacegridState state()
	{
		return s_;
	}

	@Override
	public void takeAction( final JointAction<RacegridAction> a )
	{
		final RacegridAction a0 = a.get( 0 );
		a0.doAction( s_ );
		action_history_.push( a0 );
		
		final StepAction step = new StepAction();
		step.doAction( s_ );
		action_history_.push( step );
		
//		s_.t += 1;
		assert( s_.t <= s_.T );
		assert( action_history_.size() % Nevents_ == 0 );
	}
	
	@Override
	public void untakeLastAction()
	{
//		s_.t -= 1;
		
		for( int i = 0; i < Nevents_; ++i ) {
			final RacegridAction a = action_history_.pop();
			a.undoAction( s_ );
		}
		
		assert( s_.t >= 0 );
		assert( action_history_.size() % Nevents_ == 0 );
	}

	@Override
	public long depth()
	{
		return action_history_.size();
	}

	@Override
	public long t()
	{
		return action_history_.size() / Nevents_;
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
			return new double[] { -s_.T }; // { -(s_.T - s_.t) - 10 }; //{ -10 };
		}
		else if( s_.goal ) {
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
		return Long.MAX_VALUE;
	}

	@Override
	public String detailString()
	{
		return "RacegridSimulator";
	}
	
	// -----------------------------------------------------------------------
	
	private static void testBresenham( final int x1, final int y1, final int x2, final int y2 )
	{
		System.out.println( "(" + x1 + ", " + y1 + ") -> (" + x2 + ", " + y2 + ")" );
		final Iterable<Point> line = rasterLineBresenham( x1, y1, x2, y2 );
		for( final Point p : line ) {
			System.out.println( "\t" + p );
		}
	}
	
	public static void main( final String[] args )
	{
		// Test the collision algorithms
		testBresenham( 0, 0, 0, 0 );
		testBresenham( 0, 0, 1, 1 );
		testBresenham( 0, 0, 0, 2 );
		testBresenham( 0, 0, 2, 0 );
		testBresenham( 0, 0, 10, 1 );
		testBresenham( 0, 0, 100, 1 );
		testBresenham( 0, 0, 1, 2 );
		testBresenham( 0, 0, -1, 2 );
		testBresenham( 0, 0, 1, -2 );
		testBresenham( 0, 0, -1, -2 );
		testBresenham( 0, 0, 2, 1 );
		testBresenham( 0, 0, -2, 1 );
		testBresenham( 0, 0, 2, -1 );
		testBresenham( 0, 0, -2, -1 );
		testBresenham( 1, 2, 0, 0 );
		testBresenham( -1, 2, 0, 0 );
		testBresenham( 1, -2, 0, 0 );
		testBresenham( -1, -2, 0, 0 );
		testBresenham( 2, 1, 0, 0 );
		testBresenham( -2, 1, 0, 0 );
		testBresenham( 2, -1, 0, 0 );
		testBresenham( -2, -1, 0, 0 );
	}
}
