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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.domains.voyager.LaunchAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.NothingAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.Planet;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.domains.voyager.Unit;
import edu.oregonstate.eecs.mcplan.domains.voyager.Voyager;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;
import edu.oregonstate.eecs.mcplan.util.Fn;

public class AttackPolicy extends AnytimePolicy<VoyagerState, VoyagerAction>
{
	private final Player self_;
	private final Planet target_;
	
	private final double win_margin_;
	private final String repr_;
	
	private final double combat_worker_ratio_ = 0.25;
	private final double reinforce_soldier_ratio_ = 0.5;
	
	private VoyagerState s_ = null;
	private long t_ = 0;
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder( 23, 41 )
			.append( self_ ).append( target_ ).append( win_margin_ )
			.append( combat_worker_ratio_ ).append( reinforce_soldier_ratio_ ).toHashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
//		System.out.println( "AttackPolicy.equals()" );
		if( obj == null || !(obj instanceof AttackPolicy) ) {
			return false;
		}
		final AttackPolicy that = (AttackPolicy) obj;
//		System.out.println( "\t" + toString() + ".equals( " + that.toString() + " )" );
		return self_ == that.self_
			   && target_.equals( that.target_ )
			   && win_margin_ == that.win_margin_
			   && combat_worker_ratio_ == that.combat_worker_ratio_
			   && reinforce_soldier_ratio_ == that.reinforce_soldier_ratio_;
	}
	
	public AttackPolicy( final Player self, final Planet target, final double win_margin )
	{
		self_ = self;
		target_ = target;
		win_margin_ = win_margin;
		repr_ = "AttackPolicy" + self.id + "[" + target_.id + ", " + win_margin_ + "]";
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
		final ArrayList<Planet> friendly_planets = Voyager.playerPlanets( s_, self_ );
		final ArrayList<Planet> enemy_planets = Voyager.playerPlanets( s_, self_.enemy() );
		// Consider launch actions:
		// 1. Find the closest friendly-{enemy, neutral} pair.
		//    TODO: Need to make sure it's not already targeted
		// 2. If we can safely capture from the nearest planet, do it.
		// 3. If not, transfer some Soldiers from another planet to the
		//    friendly planet.
		// 4. If all else fails, see if we would like to re-balance workers.
		if( friendly_planets.size() == 0 || enemy_planets.size() == 0 ) {
			return new NothingAction();
		}
		double nn_strength = Voyager.defense_strength( target_.population() );
		final int enemy_strength = Fn.sum( Fn.map( new Fn.IntFunction1<Planet>() {
			@Override public int apply( final Planet p ) { return Voyager.defense_strength( p.population() ); }
		}, new Fn.ArraySlice<Planet>( s_.planets ) ) );
		nn_strength += win_margin_ * enemy_strength;
		final int points_needed = Math.max( (int) Math.ceil( nn_strength ), 1 );
		final Planet near_friendly = Voyager.nearest( target_, friendly_planets );
		final int[] nf_pop = Arrays.copyOf( near_friendly.population(), Unit.values().length );
		nf_pop[Unit.Worker.ordinal()]
			= (int) Math.floor( combat_worker_ratio_ * nf_pop[Unit.Worker.ordinal()] );
		final int nf_strength = Voyager.defense_strength( nf_pop );
		final int spare_workers = Math.max( 0, nf_pop[Unit.Worker.ordinal()] - 1 );
		final int spare_soldiers = Math.max( 0, nf_pop[Unit.Soldier.ordinal()] - 1 );
		// FIXME: Sending workers is pointless since they now have 0 strength.
		// Fix it better!
		final int usable_workers = 0; //Math.min( target_.capacity, spare_workers );
		final int soldiers_needed = Math.max( 0, (int) Math.ceil(
			points_needed / (double) Unit.Soldier.attack() ) );
		if( spare_soldiers >= soldiers_needed ) {
			final int[] launch_pop = new int[Unit.values().length];
			launch_pop[Unit.Soldier.ordinal()] = soldiers_needed;
			launch_pop[Unit.Worker.ordinal()] = usable_workers;
			return new LaunchAction( near_friendly, target_, launch_pop );
		}
		
		{
			// Can't take near_nonfriendly directly; reinforce near_friendly.
			final Planet n = near_friendly;
			Collections.sort( friendly_planets, new Comparator<Planet>() {
				@Override public int compare( final Planet a, final Planet b )
				{ return (int) (Voyager.sq_distance( a, n ) - Voyager.sq_distance( b, n )); }
			} );
			for( final Planet p : friendly_planets ) {
				// TODO: Magic number 2
				if( p.id != near_friendly.id && p.population( Unit.Soldier ) > 2 ) {
					final int[] launch_pop = new int[Unit.values().length];
					launch_pop[Unit.Soldier.ordinal()]
						= (int) Math.floor( reinforce_soldier_ratio_ * p.population( Unit.Soldier ) );
					return new LaunchAction( p, near_friendly, launch_pop );
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
		return repr_;
	}
	
	@Override
	public String toString()
	{
		return repr_;
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
