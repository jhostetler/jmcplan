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
package edu.oregonstate.eecs.mcplan.domains.planetwars;

/**
 * Stores all of the random numbers needed for Zobrist hashing.
 */
public class PwHash
{
	private final PwGame game;
	
	public final int planet_numbers;
	public final int planet_pair_numbers;
	public final int Nhashes;
	
	public final int Nplanets;
	
	public final int max_eta;
	private final long[][] planet_owner;
	private final long[][][][] planet_population;
	private final long[][][] planet_carry_damage;
	private final long[][] planet_production;
	private final long[][][] planet_stored_production;
	private final long[][][][][][] ships;
	
	public PwHash( final PwGame game, final int Nplanets, final int max_eta )
	{
		this.game = game;
		this.max_eta = max_eta;
		this.Nplanets = Nplanets;
		
		final int Nplayers = PwPlayer.values().length;
		final int Nunit_types = game.Nunits();
		final int population = Nunit_types * (game.max_population + 1);
		int type_costs = 0;
		for( int i = 0; i < Nunit_types; ++i ) {
			type_costs += game.unit( i ).cost + 1;
		}
		
		// Planet stuff
		planet_numbers = Nplanets * (Nplayers + (Nunit_types + 1) + type_costs
									 + (PwPlayer.Ncompetitors * (population + game.max_hp)));
		planet_owner = new long[Nplanets][Nplayers];
		planet_population = new long[Nplanets][PwPlayer.Ncompetitors][Nunit_types][game.max_population + 1]; // Need +1 for zero
		planet_carry_damage = new long[Nplanets][PwPlayer.Ncompetitors][game.max_hp];
		planet_production = new long[Nplanets][Nunit_types + 1]; // Need one for 'null'
		planet_stored_production = new long[Nplanets][Nunit_types][];
		for( int p = 0; p < Nplanets; ++p ) {
			for( int t = 0; t < game.Nunits(); ++t ) {
				// FIXME: Didn't expect to need '+1' here; why doesn't
				// production happen immediately?
				planet_stored_production[p][t] = new long[game.unit( t ).cost + 1];
			}
		}
		
		// Spaceship stuff
		planet_pair_numbers = Nplanets * Nplanets
							* (max_eta + 1) * PwPlayer.Ncompetitors * Nunit_types * game.max_population;
		ships = new long[Nplanets][Nplanets]
					    [max_eta + 1]
						[PwPlayer.Ncompetitors]
						[Nunit_types]
						[game.max_population];
		
		Nhashes = planet_numbers + planet_pair_numbers;
		
		// Initialize Planet hash arrays
		int total = 0;
		for( int p = 0; p < Nplanets; ++p ) {
			for( int y = 0; y < PwPlayer.values().length; ++y ) {
				planet_owner[p][y] = game.rng.nextLong();
				total += 1;
			}
			
			for( int y = 0; y < PwPlayer.Ncompetitors; ++y ) {
				for( int d = 0; d < game.max_hp; ++d ) {
					planet_carry_damage[p][y][d] = game.rng.nextLong();
					total += 1;
				}
			}
			
			for( int t = 0; t < game.Nunits(); ++t ) {
				planet_production[p][t] = game.rng.nextLong();
				total += 1;
				
				for( int y = 0; y < PwPlayer.Ncompetitors; ++y ) {
					for( int pop = 0; pop <= game.max_population; ++pop ) {
						planet_population[p][y][t][pop] = game.rng.nextLong();
						total += 1;
					}
				}
				
				for( int prod = 0; prod <= game.unit( t ).cost; ++prod ) {
					planet_stored_production[p][t][prod] = game.rng.nextLong();
					total += 1;
				}
			}
			
			// Need one extra for 'null'
			planet_production[p][game.Nunits()] = game.rng.nextLong();
			total += 1;
		}
		assert( total == planet_numbers );
		
		// Initialize Spaceship hash arrays
		total = 0;
		for( int p1 = 0; p1 < Nplanets; ++p1 ) {
			for( int p2 = 0; p2 < Nplanets; ++p2 ) {
				for( int eta = 0; eta <= max_eta; ++eta ) {
					for( int player = 0; player < PwPlayer.Ncompetitors; ++player ) {
						for( int t = 0; t < game.Nunits(); ++t ) {
							for( int pop = 0; pop < game.max_population; ++pop ) {
								ships[p1][p2][eta][player][t][pop] = game.rng.nextLong();
								total += 1;
							}
						}
					}
				}
			}
		}
		assert( total == planet_pair_numbers );
	}
	
	public long hashOwner( final PwPlanet p, final PwPlayer owner )
	{
		return planet_owner[p.id][owner.id];
	}
	
	public long hashPopulation( final PwPlanet p, final PwPlayer y, final PwUnit type, final int population )
	{
		return planet_population[p.id][y.id][type.id][population];
	}
	
	public long hashCarryDamage( final PwPlanet p, final PwPlayer y, final int damage )
	{
		return planet_carry_damage[p.id][y.id][damage];
	}
	
	public long hashProduction( final PwPlanet p, final PwUnit type )
	{
		final int idx;
		if( type == null ) {
			idx = game.Nunits();
		}
		else {
			idx = type.id;
		}
		return planet_production[p.id][idx];
	}
	
	public long hashStoredProduction( final PwPlanet p, final PwUnit type, final int stored )
	{
		return planet_stored_production[p.id][type.id][stored];
	}
	
	public long hashSpaceship( final PwPlanet src, final PwPlanet dest, final int eta,
							   final PwPlayer owner, final PwUnit type, final int population )
	{
		return ships[src.id][dest.id][eta][owner.id][type.id][population];
	}
}
