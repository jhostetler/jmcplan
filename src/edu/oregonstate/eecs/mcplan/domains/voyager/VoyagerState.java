/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.Comparator;

import edu.oregonstate.eecs.mcplan.SortedList;
import edu.oregonstate.eecs.mcplan.State;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

/**
 * Represents the state of a Voyager game.
 * 
 * TODO: VoyagerState should have a createSpaceship() method, and LaunchAction
 * should call it. Then, the VoyagerHash instance doesn't have to be public.
 */
public class VoyagerState implements State<VoyagerState, VoyagerStateToken>
{
	private static class SpaceshipArrivalTimeComparator implements Comparator<Spaceship>
	{
		@Override
		public int compare( final Spaceship a, final Spaceship b )
		{
			return a.arrival_time - b.arrival_time;
		}
	}
	
	private static class SpaceshipDetailedComparator implements Comparator<Spaceship>
	{
		@Override
		public int compare( final Spaceship a, final Spaceship b )
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
	
	public final Planet[] planets;
	public final Player[] players;
	/**
	 * Think of this as a priority queue in order of arrival time. We don't
	 * use a PriorityQueue because we need additional ordering guarantees
	 * in order to implement hashing efficiently.
	 */
	public final SortedList<Spaceship> spaceships
		= new SortedList<Spaceship>( new SpaceshipDetailedComparator() );
	public final int width;
	public final int height;
	public VoyagerHash hash;
	
	public final Spaceship.Factory spaceship_factory = new Spaceship.Factory();
	
	public VoyagerState( final Planet[] planets, final Player[] players,
						 final int width, final int height, final VoyagerHash hash )
	{
		this.planets = planets;
		this.players = players;
		this.width = width;
		this.height = height;
		this.hash = hash;
	}

	@Override
	public VoyagerStateToken token()
	{
		return new VoyagerStateToken( this );
	}
	
	public double[] featureVector()
	{
		final TDoubleList fv = new TDoubleArrayList();
		
		// Planet features
		for( final Planet p : planets ) {
			addPlanetFeatures( p, fv );
		}
		
		// Ship features
		final int ship_offset = fv.size();
		final int Nplayer_ships = (hash.max_eta + 1) * Unit.values().length;
		fv.addAll( new double[hash.Nplanets * Player.competitors * Nplayer_ships] );
		final int planet_numbers = Player.competitors * Nplayer_ships;
		// Format:
		// for each planet -> for each player -> for each eta -> for each type
		for( final Spaceship ship : spaceships ) {
			final int i = ship_offset // Offset to ship features
						+ ship.dest.id * planet_numbers // Offset to dest planet
						+ ship.owner.ordinal() * Nplayer_ships // Offset to player
						+ ship.arrival_time * Unit.values().length; // Offset to eta
			for( int t = 0; t < Unit.values().length; ++t ) {
				fv.set( i + t, fv.get( i + t ) + ship.population[t] );
			}
		}
		
		return fv.toArray();
	}
	
	private void addPlanetFeatures( final Planet p, final TDoubleList fv )
	{
		for( final Player player : Player.values() ) {
			if( p.owner() == player ) {
				fv.add( 1.0 );
			}
			else {
				fv.add( 0.0 );
			}
		}
		for( final Unit type : Unit.values() ) {
			fv.add( p.population( type ) );
		}
		for( final Unit type : Unit.values() ) {
			fv.add( p.storedProduction( type ) );
		}
	}
}
