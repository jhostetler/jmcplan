/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.planetwars;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.domains.planetwars.a.PwA_ActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.planetwars.a.PwA_Units;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class PwGame
{
	public static PwGame PlanetWarsBasic( final RandomGenerator rng )
	{
		final ArrayList<PwUnit> units = new ArrayList<PwUnit>();
		for( final PwA_Units ut : PwA_Units.values() ) {
			units.add( ut.u );
		}
		
		final int T = 200;
		final int epoch = 1;
		final int max_population = 1000;
		final int planet_capacity = Integer.MAX_VALUE;
		final int[] starting_units = new int[] { 10 };
		
		return new PwGame( rng, T, epoch, units, max_population, planet_capacity, starting_units,
						   new PwA_ActionGenerator() );
	}
	
	// -----------------------------------------------------------------------
	
	public final RandomGenerator rng;
	private final ArrayList<PwUnit> units;
	
	public final int max_population;
	public final int max_hp;
	public final int max_cost;
	
	public final int planet_capacity;
	public final int[] starting_units;
	public final ActionGenerator<PwState, JointAction<PwEvent>> actions;
	
	// TODO: Make parameter
	public final int velocity = 4;
	
//	public final PwHash hash;
	
	public final int T;
	public final int epoch;
	
	/**
	 * For resolving battles, the side with fewer troops gets a penalty
	 * proportional to the numbers ratio times 'numerical_advantage'. Thus,
	 * 0 advantage means numbers don't matter, 1.0 means that a side that
	 * is outnumbered 2:1 deals half as much damage, and as advantage -> infinity,
	 * the side with more troops always wins.
	 */
	private final double numerical_advantage = 0.0;
	
	public PwGame( final RandomGenerator rng, final int T, final int epoch,
				   final ArrayList<PwUnit> units, final int max_population,
				   final int planet_capacity, final int[] starting_units,
				   final ActionGenerator<PwState, JointAction<PwEvent>> actions )
	{
		this.rng = rng;
		this.T = T;
		this.epoch = epoch;
		this.units = units;
		this.max_population = max_population;
		this.planet_capacity = planet_capacity;
		this.starting_units = starting_units;
		this.actions = actions;
		
		int max_hp = 0;
		int max_cost = 0;
		for( final PwUnit u : units ) {
			if( u.hp > max_hp ) {
				max_hp = u.hp;
			}
			if( u.cost > max_cost ) {
				max_cost = u.cost;
			}
		}
		this.max_hp = max_hp;
		this.max_cost = max_cost;
		
//		hash = new PwHash( this, map.Nplanets, map.max_eta );
	}
	
	public PwPlanet createCapitolPlanet( final int id, final int x, final int y, final PwPlayer owner )
	{
		final int[][] pop = new int[PwPlayer.Ncompetitors][Nunits()];
		Fn.memcpy( pop[owner.id], starting_units );
		final PwPlanet p = new PwPlanet( this, id, planet_capacity, pop, x, y, owner );
		p.setSetup( 0 );
		return p;
	}
	
	public PwPlanet createNeutralPlanet( final int id, final int x, final int y )
	{
		final int[][] pop = new int[PwPlayer.Ncompetitors][Nunits()];
		return new PwPlanet( this, id, planet_capacity, pop, x, y, PwPlayer.Neutral );
	}
	
	public PwUnit unit( final int i )
	{
		return units.get( i );
	}
	
	public Iterable<PwUnit> units()
	{
		return units;
	}
	
	public int Nunits()
	{
		return units.size();
	}

	public PwUnit defaultProduction()
	{
		return unit( 0 );
	}
	
	public int[] damage( final PwPlanet p )
	{
		final int[] min_pop = p.population( PwPlayer.Min );
		final int[] max_pop = p.population( PwPlayer.Max );
		// Population proportion of different unit types
		final double[] pn = Fn.vcopy_as_double( min_pop );
		Fn.normalize_inplace( pn );
		final double[] pm = Fn.vcopy_as_double( max_pop );
		Fn.normalize_inplace( pm );
		
		final int sum_min = Fn.sum( min_pop );
		final int sum_max = Fn.sum( max_pop );
		double sn = 0;
		double sm = 0;
		// For each matchup where u attacks v
		for( final PwUnit u : units() ) {
			for( final PwUnit v : units() ) {
				// u attack power against v
				final double dmg = unitAttack( u, v ); //u.attack( v );
				sn += sum_min * pn[u.id] * pm[v.id] * dmg;
				sm += sum_max * pm[u.id] * pn[v.id] * dmg;
			}
		}
		
		if( sum_min > sum_max ) {
			final double max_pop_ratio = sum_max / ((double) sum_min);
			sm -= numerical_advantage * max_pop_ratio * sm;
		}
		else if( sum_max > sum_min ) {
			final double min_pop_ratio = sum_min / ((double) sum_max);
			sn -= numerical_advantage * min_pop_ratio * sn;
		}
		
		System.out.println( "Battle: " + sn + " vs " + sm );
		final double quality = 0.8;
		sn *= (quality + (1.0 - quality)*rng.nextDouble());
		sm *= (quality + (1.0 - quality)*rng.nextDouble());
		System.out.println( "\tRandomized: " + sn + " vs " + sm );
		
		final int[] dmg = new int[] { (int) sn, (int) sm };
//		System.out.println( "Damage @ " + p.id + ": " + Arrays.toString( dmg ) );
		return dmg;
	}

	// TODO: This is where you could implement differential combat matchups.
	private double unitAttack( final PwUnit u, final PwUnit v )
	{
		return u.attack;
	}
	
	public Pair<int[], Integer> survivors( final int[] pop, final int damage )
	{
		final int[] r = Arrays.copyOf( pop, pop.length );
		int dmg = damage;
		int tpop = Fn.sum( r );
	outer:
		while( dmg > 0 && tpop > 0 ) {
			// Locate a random unit
			int i = rng.nextInt( tpop );
			for( int u = 0; u < r.length; ++u ) {
				i -= r[u];
				if( i < 0 ) {
					// If damage kills the unit, decrement population and
					// search for the next unit.
					if( dmg >= unit( u ).hp ) {
						r[u] -= 1;
						tpop -= 1;
						dmg -= unit( u ).hp;
						break;
					}
					// Else, damage is "carry damage"; no more units die.
					else {
						break outer;
					}
				}
			}
		}
		assert( dmg >= 0 );
		return Pair.makePair( r, dmg );
	}
}
