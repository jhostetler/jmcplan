/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.crossing;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class IpcCrossingState implements State
{
	public final IpcCrossingParameters params;
	/**
	 * Stored in [y][x] order, since we need to traverse rows to move the
	 * obstacles.
	 */
	public final boolean[][] grid;
	public int x = 0;
	public int y = 0;
	
	public int t = 0;
	
	public boolean goal = false;
	private boolean crashed = false;
	
	public IpcCrossingState( final IpcCrossingParameters params )
	{
		this.params = params;
		grid = new boolean[params.height][params.width];
	}
	
	public IpcCrossingState( final IpcCrossingState that )
	{
		this.params = that.params;
		this.grid = Fn.copy( that.grid );
		this.x = that.x;
		this.y = that.y;
		this.t = that.t;
		this.goal = that.goal;
		this.crashed = that.crashed;
	}
	
	@Override
	public void close()
	{ }
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "t: " ).append( t )
		  .append( ", x: " ).append( x )
		  .append( ", y: " ).append( y )
		  .append( ", goal: " ).append( goal )
		  .append( ", crashed: " ).append( crashed );
		
		for( int j = grid.length - 1; j >= 0; --j ) {
			sb.append( "\n" );
			final boolean[] row = grid[j];
			for( final boolean b : row ) {
				sb.append( b ? "X" : "." );
			}
		}
		
		return sb.toString();
	}
	
	public IpcCrossingState step( final RandomGenerator rng, final IpcCrossingAction a )
	{
		final IpcCrossingState sprime = new IpcCrossingState( params );
		sprime.t = t + 1;
		sprime.x = x;
		sprime.y = y;
		sprime.goal = goal;
		sprime.crashed = crashed;
		
		if( !crashed && !goal ) {
			if( grid[y][x] ) {
				sprime.crashed = true;
			}
			else if( x == params.goal_x && y == params.goal_y ) {
				sprime.goal = true;
			}
			else {
				switch( a ) {
					case North:
						if( sprime.y < params.height - 1 ) {
							sprime.y += 1;
						}
						break;
					case South:
						if( sprime.y > 0 ) {
							sprime.y -= 1;
						}
						break;
					case East:
						if( sprime.x < params.width - 1 ) {
							sprime.x += 1;
						}
						break;
					case West:
						if( sprime.x > 0 ) {
							sprime.x -= 1;
						}
						break;
					default: throw new AssertionError();
				}
			}
		}
		
		for( int j = 1; j < params.height - 1; ++j ) {
			final boolean[] row = grid[j];
			final boolean[] row_prime = sprime.grid[j];
			// Move obstacles
			for( int i = 0; i < params.width - 1; ++i ) {
				row_prime[i] = row[i+1];
			}
			// Spawn new obstacles
			row_prime[params.width - 1] = rng.nextDouble() < params.input_rate;
		}
		
		return sprime;
	}

	@Override
	public boolean isTerminal()
	{
		return t >= params.T;
	}
}
