/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.planetwars;

import java.util.ArrayList;
import java.util.Comparator;

import edu.oregonstate.eecs.mcplan.State;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

/**
 * @author jhostetler
 *
 */
public class PwState implements State
{
	private static class PwShipArrivalTimeComparator implements Comparator<PwShip>
	{
		@Override
		public int compare( final PwShip a, final PwShip b )
		{
			return a.arrival_time - b.arrival_time;
		}
	}
	
	private static class PwShipDetailedComparator implements Comparator<PwShip>
	{
		@Override
		public int compare( final PwShip a, final PwShip b )
		{
			final int ceta = a.arrival_time - b.arrival_time;
			if( ceta != 0 ) {
				return ceta;
			}
			final int cplayer = a.owner.id - b.owner.id;
			if( cplayer != 0 ) {
				return cplayer;
			}
			final int csrc = a.src.id - b.src.id;
			if( csrc != 0 ) {
				return csrc;
			}
			final int cdest = a.dest.id - b.dest.id;
			// This holds because only one ship can be launched per turn
			// from a planet, so (ETA, owner, src, dest) identifies the ship.
			assert( cdest != 0 );
			return cdest;
		}
	}
	
	public static int Neutral = -1;
	public static int NumPlayers = 2;
	
	public final PwGame game;
	
	public final PwPlanet[] planets;
	public final PwRoute[] routes;
	
	public final int width;
	public final int height;
	
	public int t = 0;
	
	public PwState( final PwGame game, final PwPlanet[] planets,
					final PwRoute[] routes, final int width, final int height )
	{
		this.game = game;
		this.planets = planets;
		this.routes = routes;
		this.width = width;
		this.height = height;
	}
	
	public ArrayList<PwRoute> routes( final PwPlanet planet )
	{
		final ArrayList<PwRoute> list = new ArrayList<PwRoute>();
		for( final PwRoute route : routes ) {
			if( route.a == planet || route.b == planet ) {
				list.add( route );
			}
		}
		return list;
	}
	
	public boolean connected( final PwPlanet a, final PwPlanet b )
	{
		for( final PwRoute route : routes ) {
			if( (route.a == a && route.b == b) || (route.a == b && route.b == a) ) {
				return true;
			}
		}
		return false;
	}
	
	public double[] featureVector()
	{
		final TDoubleList fv = new TDoubleArrayList();
		
		// Planet features
		for( final PwPlanet p : planets ) {
			addPlanetFeatures( p, fv );
		}
		
		// Ship features
		final int ship_offset = fv.size();
		final int Nplayer_ships = (hash.max_eta + 1) * game.Nunits();
		fv.addAll( new double[hash.Nplanets * PwPlayer.Ncompetitors * Nplayer_ships] );
		final int planet_numbers = PwPlayer.Ncompetitors * Nplayer_ships;
		// Format:
		// for each planet -> for each player -> for each eta -> for each type
		for( final PwShip ship : ships ) {
			final int i = ship_offset // Offset to ship features
						+ ship.dest.id * planet_numbers // Offset to dest planet
						+ ship.owner.ordinal() * Nplayer_ships // Offset to player
						+ ship.arrival_time * game.Nunits(); // Offset to eta
			for( int t = 0; t < game.Nunits(); ++t ) {
				fv.set( i + t, fv.get( i + t ) + ship.population[t] );
			}
		}
		
		return fv.toArray();
	}
	
	private void addPlanetFeatures( final PwPlanet p, final TDoubleList fv )
	{
		for( final PwPlayer player : PwPlayer.values() ) {
			if( p.owner() == player ) {
				fv.add( 1.0 );
			}
			else {
				fv.add( 0.0 );
			}
		}
		
		for( final PwPlayer player : PwPlayer.competitors ) {
			for( final PwUnit type : game.units() ) {
				fv.add( p.population( player, type ) );
			}
		}
		for( final PwUnit type : game.units() ) {
			fv.add( p.storedProduction( type ) );
		}
	}

	public int supply( final PwPlayer player )
	{
		int s = 0;
		for( final PwPlanet planet : planets ) {
			s += planet.supply( player );
		}
		for( final PwRoute route : routes ) {
			s += route.supply( player );
		}
		return s;
	}
	
	public PwPlayer winner()
	{
		if( supply( PwPlayer.Min ) == 0 ) {
			return PwPlayer.Max;
		}
		else if( supply( PwPlayer.Max ) == 0 ) {
			return PwPlayer.Min;
		}
		else {
			return null;
		}
	}

	@Override
	public boolean isTerminal()
	{
		return t >= game.T || winner() != null;
	}
}
