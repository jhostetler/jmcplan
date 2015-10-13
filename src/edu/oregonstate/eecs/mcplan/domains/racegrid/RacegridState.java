/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racegrid;

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

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
	
	public final int T;
	public int t = 0;
	
	/**
	 * @param terrain Row-major (y, x) order.
	 */
	public RacegridState( final TerrainType[][] terrain, final int T )
	{
		this.terrain = terrain;
		this.T = T;
		
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
	
	public RacegridState( final RacegridState that )
	{
		this.terrain = that.terrain;
		this.T = that.T;
		this.starts = that.starts;
		this.goals = that.goals;
		this.width = that.width;
		this.height = that.height;
		this.x = that.x;
		this.y = that.y;
		this.dx = that.dx;
		this.dy = that.dy;
		this.ddx = that.ddx;
		this.ddy = that.ddy;
		this.crashed = that.crashed;
		this.goal = that.goal;
		this.t = that.t;
	}
	
//	private static int nfinalized = 0;
//	@Override
//	public void finalize()
//	{
//		System.out.println( "finalize(): " + (nfinalized++) + " RacegridState" );
//	}
	
	@Override
	public void close()
	{ }
	
	public void setRandomStartState( final RandomGenerator rng )
	{
		final int start_idx = rng.nextInt( starts.size() );
		final int[] start = starts.get( start_idx );
		x = start[0];
		y = start[1];
		dx = 0;
		dy = 0;
		ddx = 0;
		ddy = 0;
		crashed = false;
		goal = false;
		t = 0;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "[" )
		  .append( "t: " ).append( t )
		  .append( ", x: " ).append( x )
		  .append( ", y: " ).append( y )
		  .append( ", dx: " ).append( dx )
		  .append( ", dy: " ).append( dy )
		  .append( ", ddx: " ).append( ddx )
		  .append( ", ddy: " ).append( ddy )
		  .append( ", crashed: " ).append( crashed )
		  .append( ", goal: " ).append( goal )
		  .append( "]" );
		return sb.toString();
	}
	
	@Override
	public boolean isTerminal()
	{
		return crashed || goal || t >= T;
	}
}
