/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.planetwars;

import edu.oregonstate.eecs.mcplan.util.Fn;


/**
 * @author jhostetler
 *
 */
public class PwRoute
{
	private final PwGame game;
	public final PwPlanet a;
	public final PwPlanet b;
	public final int length;
	
	/** Units traveling a -> b. Length -> Nplayers -> Nunits. */
	private final int[][][] ab;
	/** Ships traveling b -> a. Length -> Nplayers -> Nunits. */
	private final int[][][] ba;
	
	/** Length -> Nplayers -> Nunits -> max_population. */
	private final long[][][][] ab_hash;
	/** Length -> Nplayers -> Nunits -> max_population. */
	private final long[][][][] ba_hash;
	
	private final double[][] ab_locations;
	private final double[][] ba_locations;
	
	private long zobrist_hash = 0L;
	
	public PwRoute( final PwRoute that )
	{
		this.game = that.game;
		this.a = that.a;
		this.b = that.b;
		this.length = that.length;
		this.ab = Fn.copy( that.ab );
		this.ba = Fn.copy( that.ba );
		
		this.ab_hash = that.ab_hash;
		this.ba_hash = that.ba_hash;
		this.ab_locations = that.ab_locations;
		this.ba_locations = that.ba_locations;
	}
	
	public PwRoute( final PwGame game, final PwPlanet a, final PwPlanet b )
	{
		this.game = game;
		this.a = a;
		this.b = b;
		final int dx = b.position_x - a.position_x;
		final int dy = b.position_y - a.position_y;
		final double d = Math.sqrt( dx*dx + dy*dy );
		this.length = (int) Math.ceil( d / game.velocity );
		final double step_x = dx / ((double) length);
		final double step_y = dy / ((double) length);
		ab_locations = new double[length][2];
		ba_locations = new double[length][2];
		for( int i = 0; i < length; ++i ) {
			ab_locations[i][0] = b.position_x - i*step_x;
			ab_locations[i][1] = b.position_y - i*step_y;
			ba_locations[i][0] = a.position_x + i*step_x;
			ba_locations[i][1] = a.position_y + i*step_y;
		}
		
		// 'length' (not 'length - 1') to include destination planet
		ab = new int[length+1][PwPlayer.Ncompetitors][game.Nunits()];
		ba = new int[length+1][PwPlayer.Ncompetitors][game.Nunits()];
		
		// Generate random numbers for Zobrist hashing
		// 'length' (not 'length - 1') to include destination planet
		ab_hash = new long[length+1][PwPlayer.Ncompetitors][game.Nunits()][];
		ba_hash = new long[length+1][PwPlayer.Ncompetitors][game.Nunits()][];
		for( int i = 0; i <= length; ++i ) {
			for( int j = 0; j < PwPlayer.Ncompetitors; ++j ) {
				for( int k = 0; k < game.Nunits(); ++k ) {
					// +1 to include 0 supply
					final int Nsupply = (game.max_population / game.unit( k ).supply) + 1;
					ab_hash[i][j][k] = new long[Nsupply];
					ba_hash[i][j][k] = new long[Nsupply];
					for( int l = 0; l < Nsupply; ++l ) {
						ab_hash[i][j][k][l] = game.rng.nextLong();
						ba_hash[i][j][k][l] = game.rng.nextLong();
					}
				}
			}
		}
		
		// 'length' (not 'length - 1') to include destination planet
		for( int i = 0; i <= length; ++i ) {
			for( int j = 0; j < PwPlayer.Ncompetitors; ++j ) {
				for( int k = 0; k < game.Nunits(); ++k ) {
					// Route starts out with no units on it.
					zobrist_hash ^= ab_hash[i][j][k][0];
					zobrist_hash ^= ba_hash[i][j][k][0];
				}
			}
		}
	}
	
	public boolean occupiedAB( final PwPlayer player )
	{
		for( int t = 0; t <= length; ++t ) {
			if( occupiedAB( t, player ) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean occupiedBA( final PwPlayer player )
	{
		for( int t = 0; t <= length; ++t ) {
			if( occupiedBA( t, player ) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean occupiedAB( final int arrival_time, final PwPlayer player )
	{
		return Fn.sum( ab[arrival_time][player.id] ) > 0;
	}
	
	public boolean occupiedBA( final int arrival_time, final PwPlayer player )
	{
		return Fn.sum( ba[arrival_time][player.id] ) > 0;
	}
	
	public double[] locationAB( final int arrival_time )
	{
		return ab_locations[arrival_time];
	}
	
	public double[] locationBA( final int arrival_time )
	{
		return ba_locations[arrival_time];
	}
	
	public int populationAB( final int arrival_time, final PwPlayer player, final PwUnit unit )
	{
		return ab[arrival_time][player.id][unit.id];
	}
	
	public int[] populationAB( final int arrival_time, final PwPlayer player )
	{
		return ab[arrival_time][player.id];
	}
	
	public int populationBA( final int arrival_time, final PwPlayer player, final PwUnit unit )
	{
		return ba[arrival_time][player.id][unit.id];
	}
	
	public int[] populationBA( final int arrival_time, final PwPlayer player )
	{
		return ba[arrival_time][player.id];
	}
	
	public void setPopulationAB( final int arrival_time, final PwPlayer player, final PwUnit unit, final int pop )
	{
		ab[arrival_time][player.id][unit.id] = pop;
	}
	
	public void setPopulationBA( final int arrival_time, final PwPlayer player, final PwUnit unit, final int pop )
	{
		ba[arrival_time][player.id][unit.id] = pop;
	}
	
	public int supply( final PwPlayer player )
	{
		int s = 0;
		for( int i = 0; i <= length; ++i ) {
			for( int j = 0; j < game.Nunits(); ++j ) {
				final PwUnit u = game.unit( j );
				s += u.supply * ab[i][player.id][j];
				s += u.supply * ba[i][player.id][j];
			}
		}
		return s;
	}
	
	public long zobristHash()
	{
		return zobrist_hash;
	}
	
	public void forward()
	{
		// Advance the pipeline
		for( int i = 1; i <= length; ++i ) {
			for( int j = 0; j < PwPlayer.Ncompetitors; ++j ) {
				for( int k = 0; k < game.Nunits(); ++k ) {
					zobrist_hash ^= ab_hash[i-1][j][k][ab[i-1][j][k]];
					zobrist_hash ^= ba_hash[i-1][j][k][ba[i-1][j][k]];
					ab[i-1][j][k] = ab[i][j][k];
					ba[i-1][j][k] = ba[i][j][k];
					zobrist_hash ^= ab_hash[i-1][j][k][ab[i-1][j][k]];
					zobrist_hash ^= ba_hash[i-1][j][k][ba[i-1][j][k]];
				}
			}
		}
		
		// Set the input cell to 0
		final int end = length; // - 1;
		for( int j = 0; j < PwPlayer.Ncompetitors; ++j ) {
			for( int k = 0; k < game.Nunits(); ++k ) {
				zobrist_hash ^= ab_hash[end][j][k][ab[end][j][k]];
				zobrist_hash ^= ba_hash[end][j][k][ba[end][j][k]];
				ab[end][j][k] = 0;
				ba[end][j][k] = 0;
				zobrist_hash ^= ab_hash[end][j][k][ab[end][j][k]];
				zobrist_hash ^= ba_hash[end][j][k][ba[end][j][k]];
			}
		}
	}
	
	public void backward()
	{
		// Reverse the pipeline
		for( int i = 0; i <= length - 1; ++i ) {
			for( int j = 0; j < PwPlayer.Ncompetitors; ++j ) {
				for( int k = 0; k < game.Nunits(); ++k ) {
					zobrist_hash ^= ab_hash[i+1][j][k][ab[i+1][j][k]];
					zobrist_hash ^= ba_hash[i+1][j][k][ba[i+1][j][k]];
					ab[i+1][j][k] = ab[i][j][k];
					ba[i+1][j][k] = ba[i][j][k];
					zobrist_hash ^= ab_hash[i+1][j][k][ab[i+1][j][k]];
					zobrist_hash ^= ba_hash[i+1][j][k][ba[i+1][j][k]];
				}
			}
		}
		
		// Set the output cell to 0
		final int end = 0;
		for( int j = 0; j < PwPlayer.Ncompetitors; ++j ) {
			for( int k = 0; k < game.Nunits(); ++k ) {
				zobrist_hash ^= ab_hash[end][j][k][ab[end][j][k]];
				zobrist_hash ^= ba_hash[end][j][k][ba[end][j][k]];
				ab[end][j][k] = 0;
				ba[end][j][k] = 0;
				zobrist_hash ^= ab_hash[end][j][k][ab[end][j][k]];
				zobrist_hash ^= ba_hash[end][j][k][ba[end][j][k]];
			}
		}
	}

	public void launch( final PwPlayer player, final PwPlanet src, final int[] population )
	{
		assert( population.length == game.Nunits() );
		
		final int[][][] cells;
		final long[][][][] hash;
		if( src == a ) {
			cells = ab;
			hash = ab_hash;
		}
		else {
			assert( src == b );
			cells = ba;
			hash = ba_hash;
		}
		
		final int end = length; // - 1;
		for( int i = 0; i < game.Nunits(); ++i ) {
			zobrist_hash ^= hash[end][player.id][i][cells[end][player.id][i]];
			cells[end][player.id][i] += population[i];
			zobrist_hash ^= hash[end][player.id][i][cells[end][player.id][i]];
			
			src.decrementPopulation( player, game.unit( i ), population[i] );
		}
	}
	
	public void unlaunch( final PwPlayer player, final PwPlanet src, final int[] population )
	{
		assert( population.length == game.Nunits() );
		
		final int[][][] cells;
		final long[][][][] hash;
		if( src == a ) {
			cells = ab;
			hash = ab_hash;
		}
		else {
			assert( src == b );
			cells = ba;
			hash = ba_hash;
		}
		
		final int end = length; // - 1;
		for( int i = 0; i < game.Nunits(); ++i ) {
			zobrist_hash ^= hash[end][player.id][i][cells[end][player.id][i]];
			cells[end][player.id][i] -= population[i];
			zobrist_hash ^= hash[end][player.id][i][cells[end][player.id][i]];
			
			src.incrementPopulation( player, game.unit( i ), population[i] );
		}
	}
}
