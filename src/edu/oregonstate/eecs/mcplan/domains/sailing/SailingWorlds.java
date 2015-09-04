/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.sailing;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public class SailingWorlds
{
	public static SailingState emptyRectangle( final int V, final int T, final int width, final int height )
	{
		final String[] spec = new String[height];
		final char[] water = new char[width];
		Arrays.fill( water, 'w' );
		for( int y = 0; y < height; ++y ) {
			spec[y] = new String( water );
		}
		final SailingTerrain[][] terrain = parse( spec, 1 );
		return new SailingState( terrain, V, T );
	}
	
	public static class EmptyRectangleFactory implements SailingState.Factory
	{
		private final int V;
		private final int T;
		private final int width;
		private final int height;
		
		public EmptyRectangleFactory( final int V, final int T, final int width, final int height )
		{
			this.V = V;
			this.T = T;
			this.width = width;
			this.height = height;
		}
		
		@Override
		public SailingState create( final RandomGenerator rng )
		{
			final SailingState s0 = emptyRectangle( V, T, width, height );
			s0.setRandomStartState( rng );
			return s0;
		}
	}
	
	public static SailingState squareIsland( final int V, final int T, final int dim, final int island_dim )
	{
		final String[] spec = new String[dim];
		final char[] water = new char[dim];
		Arrays.fill( water, 'w' );
		final char[] island = new char[dim];
		Arrays.fill( island, 'w' );
		final int water_left = (dim - island_dim) / 2;
		
		for( int j = 0; j < island_dim; ++j ) {
			island[water_left + j] = 'l';
		}
		
		for( int i = 0; i < dim; ++i ) {
			if( i >= water_left && i < water_left + island_dim ) {
				spec[i] = new String( island );
			}
			else {
				spec[i] = new String( water );
			}
		}
		
		final SailingTerrain[][] terrain = parse( spec, 1 );
		return new SailingState( terrain, V, T );
	}
	
	public static class SquareIslandFactory implements SailingState.Factory
	{
		private final int V;
		private final int T;
		private final int dim;
		private final int island_dim;
		
		public SquareIslandFactory( final int V, final int T, final int dim, final int island_dim )
		{
			this.V = V;
			this.T = T;
			this.dim = dim;
			this.island_dim = island_dim;
		}
		
		@Override
		public SailingState create( final RandomGenerator rng )
		{
			final SailingState s0 = squareIsland( V, T, dim, island_dim );
			s0.setRandomStartState( rng );
			return s0;
		}
	}
	
	public static SailingState randomObstacles( final RandomGenerator rng, final double p,
												final int V, final int T, final int dim )
	{
		final String[] spec = new String[dim];
		for( int y = 0; y < dim; ++y ) {
			final char[] noisy = new char[dim];
			Arrays.fill( noisy, 'w' );
			for( int x = 0; x < dim; ++x ) {
				// Note that the y-axis is inverted at this point since the
				// parse() function is designed to accept a literal array.
				if( x == 0 && y == dim - 1 ) {
					// No obstacles in start
					continue;
				}
				if( x == dim - 1 && y == 0 ) {
					// No obstacles in goal
					continue;
				}
				if( rng.nextDouble() < p ) {
					noisy[x] = 'l';
				}
			}
			spec[y] = new String( noisy );
		}
		
		final SailingTerrain[][] terrain = parse( spec, 1 );
		return new SailingState( terrain, V, T );
	}
	
	public static class RandomObstaclesFactory implements SailingState.Factory
	{
		private final double p;
		private final int V;
		private final int T;
		private final int dim;
		
		public RandomObstaclesFactory( final double p, final int V, final int T, final int dim )
		{
			this.p = p;
			this.V = V;
			this.T = T;
			this.dim = dim;
		}
		
		@Override
		public SailingState create(final RandomGenerator rng )
		{
			final SailingState s0 = randomObstacles( rng, p, V, T, dim );
			s0.setRandomStartState( rng );
			return s0;
		}
	}
	
	private static SailingTerrain[][] parse( final String[] spec, final int scale )
	{
		final SailingTerrain[][] terrain = new SailingTerrain[spec.length*scale][];
		final int height = spec.length;
		final int width = spec[0].length();
		for( int i = 0; i < spec.length; ++i ) {
//			assert( terrain[i].length == width );
			for( int ii = 0; ii < scale; ++ii ) {
				// We use 'height - i - 1' to invert the y-axis, so that y
				// increases up and the track looks the same as the string array.
				terrain[i*scale + ii] = new SailingTerrain[spec[height - i - 1].length()*scale];
				for( int j = 0; j < spec[height - i - 1].length(); ++j ) {
					final char ij = spec[height - i - 1].charAt( j );
					for( int jj = 0; jj < scale; ++jj ) {
						if( 'w' ==  ij ) {
							terrain[i*scale + ii][j*scale + jj] = SailingTerrain.Water;
						}
						else if( 'l' == ij ) {
							terrain[i*scale + ii][j*scale + jj] = SailingTerrain.Land;
						}
						else {
							throw new IllegalArgumentException( "spec[" + i + "][" + j + "] = " + spec[i].charAt( j ) );
						}
					}
				}
			}
		}
		
		return terrain;
	}
}
