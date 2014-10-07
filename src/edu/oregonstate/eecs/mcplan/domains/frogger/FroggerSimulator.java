/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.frogger;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;

/**
 * @author jhostetler
 *
 */
public class FroggerSimulator implements UndoSimulator<FroggerState, FroggerAction>
{
	private class StepEvent extends FroggerAction
	{
		private int old_x_ = 0;
		private int old_y_ = 0;
		private Tile[][] old_grid_ = null;
		private boolean old_squashed_ = false;
		
		@Override
		public void undoAction( final FroggerState s )
		{
			assert( old_grid_ != null );
			
			s.frog_x = old_x_;
			s.frog_y = old_y_;
			s.squashed = old_squashed_;
			
			for( int i = 0; i < old_grid_.length; ++i ) {
				for( int j = 0; j < s.params.road_length; ++j ) {
					s.grid[i+1][j] = old_grid_[i][j];
				}
			}
			
			old_grid_ = null;
		}

		@Override
		public void doAction( final FroggerState s )
		{
			assert( old_grid_ == null );
			
			old_x_ = s.frog_x;
			old_y_ = s.frog_y;
			old_squashed_ = s.squashed;
			old_grid_ = new Tile[s.params.lanes][s.params.road_length];
			for( int i = 0; i < s.params.lanes; ++i ) {
				for( int j = 0; j < s.params.road_length; ++j ) {
					old_grid_[i][j] = s.grid[i+1][j];
				}
			}
			
			for( int j = s.params.road_length - 1; j >= 0; --j ) {
				for( int i = s.params.lanes - 1; i >= 0; --i ) {
					final int lane = i + 1;
					if( s.grid[lane][j] != Tile.Car ) {
						continue;
					}
					final int next_x = j + 1;
					if( next_x == s.params.road_length ) {
						s.grid[lane][j] = Tile.Empty;
					}
					else {
						final double switch_lane = rng_.nextDouble();
						if( switch_lane < s.params.lane_switch_prob ) {
							if( s.grid[lane+1][next_x] == Tile.Empty ) {
								s.grid[lane+1][next_x] = Tile.Car;
								s.grid[lane][j] = Tile.Empty;
							}
							else if( s.grid[lane][next_x] == Tile.Empty ) {
								s.grid[lane][next_x] = Tile.Car;
								s.grid[lane][j] = Tile.Empty;
							}
						}
						else if( switch_lane > (1.0 - s.params.lane_switch_prob) ) {
							if( s.grid[lane-1][next_x] == Tile.Empty ) {
								s.grid[lane-1][next_x] = Tile.Car;
								s.grid[lane][j] = Tile.Empty;
							}
							else if( s.grid[lane][next_x] == Tile.Empty ) {
								s.grid[lane][next_x] = Tile.Car;
								s.grid[lane][j] = Tile.Empty;
							}
						}
						else if( s.grid[lane][next_x] == Tile.Empty ) {
							s.grid[lane][next_x] = Tile.Car;
							s.grid[lane][j] = Tile.Empty;
						}
					}
				}
			}
			
			for( int i = 0; i < s.params.lanes; ++i ) {
				final int lane = i + 1;
				if( s.grid[lane][0] == Tile.Empty ) {
					final double spawn = rng_.nextDouble();
					if( spawn < s.params.arrival_prob[i] ) {
						s.grid[lane][0] = Tile.Car;
					}
				}
			}
			
			if( s.grid[s.frog_y][s.frog_x] == Tile.Car ) {
				s.squashed = true;
			}
			
			if( s.squashed ) {
				s.frog_y = 0;
				s.frog_x = rng_.nextInt( s.params.road_length );
			}
		}

		@Override
		public boolean isDone()
		{
			return old_grid_ != null;
		}

		@Override
		public FroggerAction create()
		{
			return new StepEvent();
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final RandomGenerator rng_;
	private final FroggerState s_;
	
	private final Deque<FroggerAction> action_history_ = new ArrayDeque<FroggerAction>();
	private final int Nevents_ = 2;
	
	public FroggerSimulator( final RandomGenerator rng, final FroggerState s )
	{
		rng_ = rng;
		s_ = s;
		
		s_.frog_x = rng.nextInt( s.params.road_length );
		for( int t = 0; t < s.params.road_length; ++t ) {
			final StepEvent step = new StepEvent();
			step.doAction( s );
		}
	}
	
	@Override
	public FroggerState state()
	{
		return s_;
	}

	@Override
	public void takeAction( final JointAction<FroggerAction> a )
	{
		final FroggerAction a0 = a.get( 0 );
		a0.doAction( s_ );
		action_history_.push( a0 );
		
		final StepEvent step = new StepEvent();
		step.doAction( s_ );
		action_history_.push( step );
		
		s_.t += 1;
		assert( s_.t <= s_.T );
		assert( action_history_.size() % Nevents_ == 0 );
	}
	
	@Override
	public void untakeLastAction()
	{
		s_.t -= 1;
		assert( s_.t >= 0 );
		
		for( int i = 0; i < Nevents_; ++i ) {
			final FroggerAction a = action_history_.pop();
			a.undoAction( s_ );
		}
		
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
		if( s_.squashed ) {
			// Getting squashed gives a penalty and causes the agent to be
			// unable to move. The additional penalty for time remaining
			// prevents the agent from wanting to get squashed faster in order
			// to avoid per-move penalties.
			return new double[] { -20 - (s_.T - s_.t) };
		}
		else
		if( s_.goal ) {
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
		return "FroggerSimulator";
	}

}
