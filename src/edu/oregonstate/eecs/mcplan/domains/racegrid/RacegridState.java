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
	public final ArrayList<int[]> goals;
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
	
	public final int T = 40;
	public int t = 0;
	
	/**
	 * @param terrain Row-major (y, x) order.
	 */
	public RacegridState( final TerrainType[][] terrain )
	{
		this.terrain = terrain;
		starts = new ArrayList<int[]>();
		goals = new ArrayList<int[]>();
		height = terrain.length;
		width = terrain[0].length;
		for( int i = 0; i < terrain.length; ++i ) {
			for( int j = 0; j < terrain[i].length; ++j ) {
				if( terrain[i][j] == TerrainType.Start ) {
					starts.add( new int[] { j, i } );
				}
				else if( terrain[i][j] == TerrainType.Goal ) {
					goals.add( new int[] { j, i } );
				}
			}
		}
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "[" )
		  .append( "x: " ).append( x )
		  .append( ", y: " ).append( y )
		  .append( ", dx: " ).append( dx )
		  .append( ", dy: " ).append( dy )
		  .append( ", ddx: " ).append( ddx )
		  .append( ", ddy: " ).append( ddy )
		  .append( "]" );
		return sb.toString();
	}
	
	@Override
	public boolean isTerminal()
	{
		return crashed || goal || t >= T;
	}
}
