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
package edu.oregonstate.eecs.mcplan.domains.voyager.policies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.domains.voyager.LaunchAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.NothingAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.Planet;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.domains.voyager.SetProductionAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.Unit;
import edu.oregonstate.eecs.mcplan.domains.voyager.Voyager;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;

/**
 * @author jhostetler
 *
 */
public class AggressivePolicy extends Policy<VoyagerState, VoyagerAction>
{
	private final Player player_;
	private final int garrison_;
	
	private VoyagerState s_ = null;
	private long t_ = 0L;
	
	public AggressivePolicy( final Player player, final int garrison )
	{
		player_ = player;
		garrison_ = garrison;
	}
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder( 607, 613 ).append( player_ ).append( garrison_ ).toHashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof AggressivePolicy) ) {
			return false;
		}
		final AggressivePolicy that = (AggressivePolicy) obj;
		return player_ == that.player_ && garrison_ == that.garrison_;
	}

	@Override
	public void setState( final VoyagerState s, final long t )
	{
		s_ = s;
		t_ = t;
	}

	@Override
	public VoyagerAction getAction()
	{
		// Locate a large friendly force
		final ArrayList<Planet> friendly = Voyager.playerPlanets( s_, player_ );
		Collections.sort( friendly, new Comparator<Planet>() {
			@Override
			public int compare( final Planet a, final Planet b )
			{ return b.population( Unit.Soldier ) - a.population( Unit.Soldier ); }
		} );
		
		final Planet primary = (friendly.isEmpty() ? null : friendly.get( 0 ));
		final Planet secondary = (friendly.size() <= 1 ? null : friendly.get( 1 ));
		final int primary_force = (primary != null ? primary.population( Unit.Soldier ) - garrison_ : 0);
		final int secondary_force = (secondary != null ? secondary.population( Unit.Soldier ) - garrison_ : 0);
		final int force = primary_force + secondary_force;
		
		if( primary != null ) {
			// Sort enemy planets by defense strength per worker
			final ArrayList<Planet> enemy = Voyager.playerPlanets( s_, player_.enemy() );
			Collections.sort( enemy, new Comparator<Planet>() {
				@Override
				public int compare( final Planet a, final Planet b )
				{
					final int[] apop = Voyager.effectivePopulation( s_, a, player_.enemy() );
					final int[] bpop = Voyager.effectivePopulation( s_, b, player_.enemy() );
					final double aratio = apop[Unit.Worker.ordinal()] > 0
										? Voyager.defense_strength( apop ) / apop[Unit.Worker.ordinal()]
										: Double.MAX_VALUE;
					final double bratio = bpop[Unit.Worker.ordinal()] > 0
										? Voyager.defense_strength( bpop ) / bpop[Unit.Worker.ordinal()]
										: Double.MAX_VALUE;
					return (int) Math.signum( aratio - bratio );
				}
			} );
			// Find the enemy Planet with the best worker ratio that we have
			// enough forces to take over.
			for( final Planet p : enemy ) {
				final int[] pop = Voyager.effectivePopulation( s_, p, player_.enemy() );
				final int d = Voyager.defense_strength( pop );
				final int a = Voyager.attack_strength( new int[] { 0, primary_force } );
				if( a > d ) {
					return new LaunchAction( primary, p, new int[] { 0, primary_force } );
				}
			}
			
			// If there isn't a promising attack available, shift some soldiers
			for( final Planet p : friendly ) {
				if( !p.equals( primary ) && !p.equals( secondary ) ) {
					final int soldiers = p.population( Unit.Soldier ) - garrison_;
					if( soldiers > 0 ) {
						return new LaunchAction( p, primary, new int[] { 0, soldiers } );
					}
				}
			}
		}
		
		// If there are no soldiers to shift, optimize production
		for( final Planet p : friendly ) {
			if( p.nextProduced() == Unit.Worker && p.population( Unit.Worker ) >= p.capacity ) {
				return new SetProductionAction( p, Unit.Soldier );
			}
		}
		
		return new NothingAction();
	}

	@Override
	public void actionResult( final VoyagerState sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{
		return "AggressivePolicy";
	}

}
