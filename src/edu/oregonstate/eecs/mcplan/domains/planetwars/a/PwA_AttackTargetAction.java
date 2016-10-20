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
package edu.oregonstate.eecs.mcplan.domains.planetwars.a;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.domains.planetwars.PwEvent;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwLaunchAction;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwPlanet;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwPlayer;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwState;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.PreferredNumbers;

/**
 * @author jhostetler
 *
 */
public class PwA_AttackTargetAction extends PwEvent
{
	public final PwPlayer player;
	public final int target_id;
	
	public PwA_AttackTargetAction( final PwPlayer player, final int target_id )
	{
		this.player = player;
		this.target_id = target_id;
	}
	
	@Override
	public void undoAction( final PwState s )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void doAction( final RandomGenerator rng, final PwState s )
	{
		// Find neighbor of 'target' controlled by 'player' with maximum
		// population.
		int max_pop = 0;
		PwPlanet max_planet = null;
		for( final PwPlanet p : Fn.in( s.neighbors( s.planets[target_id] ) ) ) {
			if( p.owner() == player ) {
				final int pop = p.population( player, PwA_Units.Unit.u );
				if( pop > max_pop ) {
					max_pop = pop;
					max_planet = p;
				}
			}
		}
		
		if( max_planet != null ) {
			// Determine attack size
			int pop = 0;
			final Fn.IntSlice numbers = PreferredNumbers.Series_1_2_5();
			while( numbers.hasNext() ) {
				final int n = numbers.next();
				if( n < max_pop ) {
					pop = n;
				}
				else {
					break;
				}
			}
			
			// Launch attack
			if( pop > 0 ) {
				final PwState.RouteEdge e = s.route_graph.getEdge( max_planet.id, target_id );
				final PwLaunchAction launch = new PwLaunchAction(
					player, max_planet, s.routes[e.route_id], new int[] { pop } );
				launch.doAction( s );
			}
		}
	}

	@Override
	public boolean isDone()
	{
		return false;
	}

	@Override
	public PwA_AttackTargetAction create()
	{
		return new PwA_AttackTargetAction( player, target_id );
	}

	@Override
	public int hashCode()
	{
		return 3 * (5 + player.id * (7 + target_id));
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof PwA_AttackTargetAction) ) {
			return false;
		}
		final PwA_AttackTargetAction that = (PwA_AttackTargetAction) obj;
		return player == that.player && target_id == that.target_id;
	}

	@Override
	public String toString()
	{
		return "AttackAction[" + player + "; " + target_id + "]";
	}
}
