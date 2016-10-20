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
package edu.oregonstate.eecs.mcplan.domains.sailing;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class SailingState implements State
{
	public static interface Factory
	{
		public abstract SailingState create( final RandomGenerator rng );
	}
	
	/** This must be an even integer. Note that changing it will break SailingAction. */
	public static final int Nwind_directions = 8;
	public static final int reference_v = 10;
	
	public final SailingTerrain[][] terrain;
	public final int width;
	public final int height;
	
	public int x = 0;
	public int y = 0;
	
	/** The direction that the wind is blowing *from*. */
	public int w = 0;
	/** Wind speed. */
	public int v = 0;
	public final int V;
	
	public int t = 0;
	public final int T;
	
	public SailingState( final SailingTerrain[][] terrain, final int V, final int T )
	{
		this.terrain = terrain;
		this.height = terrain.length;
		this.width = terrain[0].length;
		assert( V % 2 == 1 );
		this.V = V;
		this.T = T;
	}
	
	public SailingState( final SailingState s )
	{
		this( s.terrain, s.V, s.T );
		this.x = s.x;
		this.y = s.y;
		this.w = s.w;
		this.v = s.v;
		this.t = s.t;
	}
	
	@Override
	public void close()
	{ }
	
	public double max_speed()
	{
		final double max_speed = 7.5;
		final int max_v = reference_v + (V / 2);
		return (max_speed * max_v) / reference_v;
	}

	public void setRandomStartState( final RandomGenerator rng )
	{
		x = 0;
		y = 0;
		t = 0;
		randomizeWind( rng );
	}
	
	public void randomizeWind( final RandomGenerator rng )
	{
		// Uniform wind
//		w = rng.nextInt( Nwind_directions );
		
		// Random walk wind
		final int inc = rng.nextInt( 3 ) - 1;
		w = Fn.mod( (w + inc), SailingState.Nwind_directions );
		
		// Wind speed is uniform, but centered at 10 (since V must be odd)
		final int mid = V / 2;
		final int dv = rng.nextInt( V ) - mid;
		v = reference_v + dv;
	}
	
	private static final int[] neighbor_x = new int[] { 1, 1, 0, -1, -1, -1, 0, 1 };
	private static final int[] neighbor_y = new int[] { 0, 1, 1, 1, 0, -1, -1, -1 };
	
	public boolean isNeighborObstacle( final int direction )
	{
		final int nx = x + neighbor_x[direction];
		final int ny = y + neighbor_y[direction];
		
		return nx < 0 || nx >= width || ny < 0 || ny >= height
			   || terrain[ny][nx] == SailingTerrain.Land;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "[" )
		  .append( "t: " ).append( t )
		  .append( ", x: " ).append( x )
		  .append( ", y: " ).append( y )
		  .append( ", w: " ).append( w )
		  .append( ", v: " ).append( v )
		  .append( ", goal: " ).append( x == width - 1 && y == height - 1 )
		  .append( "]" );
		
		for( int y = height - 1; y >= 0; --y ) {
			sb.append( "\n" );
			for( int x = 0; x < width; ++x ) {
				if( this.x == x && this.y == y ) {
					sb.append( "B" );
				}
				else if( terrain[y][x] == SailingTerrain.Land ) {
					sb.append( "X" );
				}
				else {
					sb.append( "." );
				}
			}
		}
		
		return sb.toString();
	}
	
	@Override
	public boolean isTerminal()
	{
		return t >= T || (x == width - 1 && y == height - 1);
	}
}
