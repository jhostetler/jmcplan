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

package edu.oregonstate.eecs.mcplan.domains.voyager.policies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.domains.voyager.LaunchAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.NothingAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.Planet;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.domains.voyager.Spaceship;
import edu.oregonstate.eecs.mcplan.domains.voyager.Unit;
import edu.oregonstate.eecs.mcplan.domains.voyager.Voyager;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;

public class DefensePolicy extends AnytimePolicy<VoyagerState, VoyagerAction>
{
	private final Player self_;
	
	private VoyagerState s_ = null;
	private long t_ = 0;
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder( 31, 47 ).append( self_ ).toHashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
//		System.out.println( "DefensePolicy.equals()" );
		if( obj == null || !(obj instanceof DefensePolicy) ) {
			return false;
		}
		final DefensePolicy that = (DefensePolicy) obj;
//		System.out.println( "\t" + toString() + ".equals( " + that.toString() + " )" );
		return self_ == that.self_;
	}
	
	public DefensePolicy( final Player self )
	{
		self_ = self;
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
		final ArrayList<Planet> friendly = Voyager.playerPlanets( s_, self_ );
		final NavigableMap<Planet, ArrayList<Spaceship>> incoming
			= new TreeMap<Planet, ArrayList<Spaceship>>();
		for( final Spaceship ship : s_.spaceships ) {
			ArrayList<Spaceship> ships = incoming.get( ship.dest );
			if( ships == null ) {
				ships = new ArrayList<Spaceship>();
				incoming.put( ship.dest, ships );
			}
			ships.add( ship );
		}
		for( final ArrayList<Spaceship> ships : incoming.values() ) {
			Collections.sort( ships, Spaceship.ArrivalTimeComparator );
		}
		final ArrayList<Pair<Integer, Planet>> events = new ArrayList<Pair<Integer, Planet>>();
		for( final Map.Entry<Planet, ArrayList<Spaceship>> ships : incoming.entrySet() ) {
			int t = 0;
			int strength_balance = Voyager.defense_strength( ships.getKey().population() );
			for( final Spaceship ship : ships.getValue() ) {
				if( ship.owner == ships.getKey().owner() ) {
					strength_balance += Voyager.defense_strength( ship.population );
				}
				else {
					// TODO: 2.0 is "safety margin"; should be a parameter
					strength_balance -= 2.0 * Voyager.attack_strength( ship.population );
					if( strength_balance <= 0 ) {
						t = ship.arrival_time;
						break;
					}
				}
			}
			if( t > 0 ) {
				events.add( Pair.makePair( t, ships.getKey() ) );
				friendly.remove( ships.getKey() );
			}
		}
		Collections.sort( events, new Pair.Comparator<Integer, Planet>() );
		for( final Pair<Integer, Planet> e : events ) {
			for( final Planet src : friendly ) {
				// TODO: Need to account for ship speed
				if( Voyager.distance( src, e.second ) < e.first ) {
					// TODO: Garrison 1 should be parameter
					final int spare_soldiers = src.population( Unit.Soldier ) - 1;
					if( spare_soldiers > 0 ) {
						final int[] pop = new int[Unit.values().length];
						pop[Unit.Soldier.ordinal()] = spare_soldiers;
						return new LaunchAction( src, e.second, pop );
					}
				}
			}
		}
		
		return new NothingAction();
	}

	@Override
	public void actionResult( final VoyagerState sprime, final double[] r )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName()
	{
		return "DefensePolicy" + self_.id;
	}
	
	@Override
	public String toString()
	{
		return getName();
	}

	@Override
	public long minControl()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long maxControl()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public VoyagerAction getAction( final long control )
	{
		return getAction();
	}

}
