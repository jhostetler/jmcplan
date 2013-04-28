/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

/**
 * @author jhostetler
 *
 */
public final class Voyager
{
	public static int production( final Planet p )
	{
		return Math.min( p.capacity, p.population( EntityType.Worker ) );
	}
	
	public static double sq_distance( final Planet a, final Planet b )
	{
		final double xdiff = a.x - b.x;
		final double ydiff = a.y - b.y;
		return xdiff*xdiff + ydiff*ydiff;
	}
	
	public static int[] playerTotalPops( final VoyagerState s )
	{
		final int[] result = new int[Player.competitors];
		for( final Planet p : s.planets ) {
			if( p.owner() != Player.Neutral ) {
				for( final EntityType type : EntityType.values() ) {
					result[p.owner().ordinal()] += p.population( type );
				}
			}
		}
		for( final Spaceship ship : s.spaceships ) {
			result[ship.owner.ordinal()] += ship.population();
		}
		return result;
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
}
