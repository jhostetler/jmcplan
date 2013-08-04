/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.Comparator;

import edu.oregonstate.eecs.mcplan.SortedList;
import edu.oregonstate.eecs.mcplan.State;

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
}
