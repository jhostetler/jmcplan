/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.ArrayList;
import java.util.List;

import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public final class Voyager
{
	public static int production( final Planet p )
	{
		return Math.min( p.capacity, p.population( Unit.Worker ) );
	}
	
	public static double investment( final Planet p, final Unit type )
	{
		return p.storedProduction( type ) / (double) production( p );
	}
	
	public static double distance( final Planet a, final Planet b )
	{
		return Math.sqrt( sq_distance( a, b ) );
	}
	
	public static double sq_distance( final Planet a, final Planet b )
	{
		final double xdiff = a.x - b.x;
		final double ydiff = a.y - b.y;
		return xdiff*xdiff + ydiff*ydiff;
	}
	
	public static Planet nearest( final Planet target, final List<Planet> ps )
	{
		double d = Double.MAX_VALUE;
		Planet closest = null;
		for( final Planet p : ps ) {
			final double dprime = sq_distance( target, p );
			if( dprime < d ) {
				d = dprime;
				closest = p;
			}
		}
		return closest;
	}
	
	/**
	 * Calculates the *attacker's* probability of winning one round of combat.
	 * Rounds are fought between the highest strength pair of units, but
	 * the combat odds depend on the relative strength of the entire force.
	 * The effect of this is to make units relatively more powerful when
	 * their side has a numerical advantage.
	 * 
	 * The 'temperature' parameter controls the size of the numerical advantage
	 * effect. High temperature makes numerical advantage less important. The
	 * 'defender_advantage' parameter favors the defender when *less than 1*.
	 * @param a
	 * @param d
	 * @return
	 */
	public static double winProbability( final int a, final int d )
	{
		// TOOD: Parameters should be in VoyagerParameters
		final double defender_advantage = 0.95;
		return winProbability( a, d, defender_advantage );
	}
	
	/**
	 * Calculates the first argument's win probability in a matchup with
	 * no defender's advantage.
	 * @param a
	 * @param b
	 * @return
	 */
	public static double jumpProbability( final int a, final int b )
	{
		return winProbability( a, b, 1.0 );
	}
	
	private static double winProbability( final int a, final int d, final double dadv )
	{
		// TOOD: Parameters should be in VoyagerParameters
		// The 'temperature' parameter controls the size of the numerical
		// advantage effect. High temperature makes numerical advantage less
		// important. The 'defender_advantage' parameter favors the defender
		// when *less than 1*.
		final double temperature = 0.5;
		final double p = 1.0 / (1.0 + Math.exp( -(1.0 / temperature) * (a - d) / (a + d) ));
		return dadv * p;
	}
	
	public static ArrayList<Planet> playerPlanets( final VoyagerState s, final Player p )
	{
		final ArrayList<Planet> planets = new ArrayList<Planet>();
		for( final Planet planet : s.planets ) {
			if( planet.owner() == p ) {
				planets.add( planet );
			}
		}
		return planets;
	}
	
	public static int playerPopulation( final VoyagerState s, final Player player )
	{
		int pop = 0;
		for( final Planet p : s.planets ) {
			if( p.owner() == player ) {
				pop += p.totalPopulation();
			}
		}
		for( final Spaceship ship : s.spaceships ) {
			if( ship.owner == player ) {
				pop += ship.population();
			}
		}
		return pop;
	}
	
	public static int[] playerTotalPops( final VoyagerState s )
	{
		final int[] result = new int[Player.competitors];
		for( final Planet p : s.planets ) {
			if( p.owner() != Player.Neutral ) {
				for( final Unit type : Unit.values() ) {
					result[p.owner().ordinal()] += p.population( type );
				}
			}
		}
		for( final Spaceship ship : s.spaceships ) {
			result[ship.owner.ordinal()] += ship.population();
		}
		return result;
	}
	
	public static int attack_strength( final int[] force )
	{
		assert( force.length == Unit.values().length );
		int s = 0;
		for( final Unit type : Unit.values() ) {
			s += type.attack() * force[type.ordinal()];
		}
		return s;
	}
	
	public static int defense_strength( final int[] force )
	{
		assert( force.length == Unit.values().length );
		int s = 0;
		for( final Unit type : Unit.values() ) {
			s += type.defense() * force[type.ordinal()];
		}
		return s;
	}
	
	public static Player winner( final VoyagerState s )
	{
		final int[] result = playerTotalPops( s );
		if( result[Player.Min.ordinal()] == 0 ) {
			return Player.Max;
		}
		else if( result[Player.Max.ordinal()] == 0 ) {
			return Player.Min;
		}
		else {
			return null;
		}
	}
	
	public static int enroute( final VoyagerState s, final Planet dest, final Player player, final Unit type )
	{
		int total = 0;
		for( final Spaceship ship : s.spaceships ) {
			if( ship.owner == player && ship.dest.equals( dest ) ) {
				total += ship.population[type.ordinal()];
			}
		}
		return total;
	}
	
	/**
	 * Calculates the effective population of a Planet for its owner, counting
	 * any inbound Spaceships. Units not belonging to 'p.owner() are not
	 * counted.
	 * @param s
	 * @param p
	 * @return
	 */
	public static int[] effectiveFriendlyPopulation( final VoyagerState s, final Planet p )
	{
		final int[] pop = new int[Unit.values().length];
		Fn.memcpy( pop, p.population(), Unit.values().length );
		final Player player = p.owner();
		for( final Spaceship ship : s.spaceships ) {
			if( ship.owner == player && ship.dest.equals( p ) ) {
				Fn.vplus_inplace( pop, ship.population );
			}
		}
		return pop;
	}
	
	/**
	 * Calculates the effective population of a Planet for 'player', counting
	 * any inbound Spaceships. Units not belonging to 'player' are not counted.
	 * @param s
	 * @param p
	 * @param player
	 * @return
	 */
	public static int[] effectivePopulation( final VoyagerState s, final Planet p, final Player player )
	{
		final int[] pop = new int[Unit.values().length];
		if( p.owner() == player ) {
			Fn.memcpy( pop, p.population(), Unit.values().length );
		}
		for( final Spaceship ship : s.spaceships ) {
			if( ship.owner == player && ship.dest.equals( p ) ) {
				Fn.vplus_inplace( pop, ship.population );
			}
		}
		return pop;
	}
	
	/**
	 * Calculates the effective population of a Planet, counting any inbound
	 * Spaceships. Could be negative if enemy ships are inbound.
	 * @param s
	 * @param p
	 * @return
	 */
	public static int[] effectivePopulation( final VoyagerState s, final Planet p )
	{
		final int[] pop = new int[Unit.values().length];
		Fn.memcpy( pop, p.population(), Unit.values().length );
		for( final Spaceship ship : s.spaceships ) {
			if( ship.dest.equals( p ) ) {
				Fn.vplus_inplace( pop, ship.population );
			}
		}
		return pop;
	}
	
	public static Pair<Unit, Unit> minimaxMatchup( final int[] a, final int[] d )
	{
		double max_p = -Double.MAX_VALUE;
		int max_i = -1;
		int min_j = -1;
		for( int i = 0; i < a.length; ++i ) {
			if( a[i] == 0 ) {
				continue;
			}
			double min_p = Double.MAX_VALUE;
			for( int j = 0; j < d.length; ++j ) {
				if( d[j] == 0 ) {
					continue;
				}
				if( min_p > Unit.attack_matchups[i][j] ) {
					min_p = Unit.attack_matchups[i][j];
					min_j = j;
				}
			}
			if( min_p > max_p ) {
				max_p = min_p;
				max_i = i;
			}
		}
		assert( max_i >= 0 );
		assert( min_j >= 0 );
		return Pair.makePair( Unit.values()[max_i], Unit.values()[min_j] );
	}
}
