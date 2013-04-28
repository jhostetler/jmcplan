/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.Comparator;
import java.util.PriorityQueue;

import edu.oregonstate.eecs.mcplan.experiments.Copyable;

/**
 * @author jhostetler
 *
 */
public class VoyagerState implements Copyable<VoyagerState>
{
	private static class SpaceshipArrivalTimeComparator implements Comparator<Spaceship>
	{
		@Override
		public int compare( final Spaceship a, final Spaceship b )
		{
			return a.arrival_time - b.arrival_time;
		}
	}
	
	public static int Neutral = -1;
	public static int NumPlayers = 2;
	
	public final Planet[] planets;
	public final Player[] players;
	public final PriorityQueue<Spaceship> spaceships
		= new PriorityQueue<Spaceship>( 11, new SpaceshipArrivalTimeComparator() );
	public final int width;
	public final int height;
	
	public VoyagerState( final Planet[] planets, final Player[] players, final int width, final int height )
	{
		this.planets = planets;
		this.players = players;
		this.width = width;
		this.height = height;
	}

	@Override
	public VoyagerState copy()
	{
		return new VoyagerState( planets, players, width, height );
	}
}
