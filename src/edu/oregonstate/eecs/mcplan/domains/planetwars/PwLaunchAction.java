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

import java.util.Arrays;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.math3.random.RandomGenerator;

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
	public void doAction( final RandomGenerator rng, final PwState s )
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
