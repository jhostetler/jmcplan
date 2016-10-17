/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.frogger;

import edu.oregonstate.eecs.mcplan.State;

/**
 * @author jhostetler
 *
 */
public class FroggerState implements State
{
	public final FroggerParameters params;
	public final Tile[][] grid;
	
	public int frog_x = 0;
	public int frog_y = 0;
	
	public boolean goal = false;
	public boolean squashed = false;
	
	public final int T = 100;
	public int t = 0;
	
	public FroggerState( final FroggerParameters params )
	{
		this.params = params;
		grid = new Tile[params.lanes + 2][params.road_length];
		
		for( int j = 0; j < params.road_length; ++j ) {
			grid[0][j] = Tile.Start;
			grid[grid.length - 1][j] = Tile.Goal;
			
			for( int i = 1; i <= params.lanes; ++i ) {
				grid[i][j] = Tile.Empty;
			}
		}
	}
	
	@Override
	public boolean isTerminal()
	{
		return goal || t >= T;
	}

	@Override
	public void close()
	{ }

}
