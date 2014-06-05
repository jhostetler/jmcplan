/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racegrid;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.State;

/**
 * @author jhostetler
 *
 */
public class RacegridState implements State
{
	public final TerrainType[][] terrain;
	public final ArrayList<int[]> starts;
	public final int width;
	public final int height;
	public int x = 0;
	public int y = 0;
	public int dx = 0;
	public int dy = 0;
	public int ddx = 0;
	public int ddy = 0;
	
	public boolean crashed = false;
	public boolean goal = false;
	
	public final int T = 100;
	public int t = 0;
	
	public RacegridState( final TerrainType[][] terrain )
	{
		this.terrain = terrain;
		starts = new ArrayList<int[]>();
		height = terrain.length;
		width = terrain[0].length;
		for( int i = 0; i < terrain.length; ++i ) {
			for( int j = 0; j < terrain[i].length; ++j ) {
				if( terrain[i][j] == TerrainType.Start ) {
					starts.add( new int[] { j, i } );
				}
			}
		}
	}
	
	@Override
	public boolean isTerminal()
	{
		return goal || t >= T;
	}
}
