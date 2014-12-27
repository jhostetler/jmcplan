/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.planetwars;

import java.util.Arrays;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author jhostetler
 *
 */
public class PwLaunchAction extends PwEvent
{
	public final PwPlayer player;
	public final PwPlanet src;
	public final PwRoute route;
	public final int[] population;
	
	private boolean done = false;
	
	public PwLaunchAction( final PwPlayer player, final PwPlanet src, final PwRoute route, final int[] population )
	{
		this.player = player;
		this.src = src;
		this.route = route;
		this.population = population;
	}
	
	@Override
	public void undoAction( final PwState s )
	{
		assert( done );
		route.unlaunch( player, src, population );
		done = false;
	}

	@Override
	public void doAction( final PwState s )
	{
		assert( !done );
		route.launch( player, src, population );
		done = true;
	}

	@Override
	public boolean isDone()
	{
		return done;
	}

	@Override
	public PwLaunchAction create()
	{
		return new PwLaunchAction( player, src, route, population );
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder( 3, 7 )
			.append( player ).append( src ).append( route ).append( population ).toHashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof PwLaunchAction) ) {
			return false;
		}
		final PwLaunchAction that = (PwLaunchAction) obj;
		return player == that.player
			   && src == that.src
			   && route == that.route
			   && Arrays.equals( population, that.population );
	}

	@Override
	public String toString()
	{
		return new StringBuilder()
			.append( "PwLaunchAction[" ).append( player )
			.append( "; " ).append( src )
			.append( "; " ).append( route )
			.append( "; " ).append( Arrays.toString( population ) )
			.append( "]" ).toString();
	}
}
