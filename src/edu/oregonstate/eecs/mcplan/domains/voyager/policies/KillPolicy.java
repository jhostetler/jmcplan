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
import java.util.List;

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

/**
 * Launches attacks at the nearest enemy planet. Intended for use when we
 * have an overwhelming advantage and just want to end the game.
 */
public class KillPolicy extends AnytimePolicy<VoyagerState, VoyagerAction>
{
	private final Player self_;
	private final int garrison_;
	
	private VoyagerState s_ = null;
	private long t_ = 0;
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder( 43, 61 ).append( self_ ).append( garrison_ ).toHashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
//		System.out.println( "KillPolicy.equals()" );
		if( obj == null || !(obj instanceof KillPolicy) ) {
			return false;
		}
		final KillPolicy that = (KillPolicy) obj;
//		System.out.println( "\t" + toString() + ".equals( " + that.toString() + " )" );
		return self_ == that.self_
			   && garrison_ == that.garrison_;
	}
	
	public KillPolicy( final Player self, final int garrison )
	{
		self_ = self;
		garrison_ = garrison;
	}
	
	@Override
	public void setState( final VoyagerState s, final long t )
	{
		s_ = s;
		t_ = t;
	}
	
	private double targetWeight( final Planet t, final List<Planet> friendly_planets )
	{
		double w = 0.0;
		for( final Planet f : friendly_planets ) {
			w += Voyager.distance( f, t ) * Math.max( 0, f.population( Unit.Soldier ) - garrison_ );
		}
		return 1.0 / w;
	}

	@Override
	public VoyagerAction getAction()
	{
		final ArrayList<Planet> friendly = Voyager.playerPlanets( s_, self_ );
		final ArrayList<Planet> enemy = Voyager.playerPlanets( s_, self_.enemy() );
		if( enemy.isEmpty() ) {
			return new NothingAction();
		}
		Planet target = null;
		double weight = 0;
		for( final Planet t : enemy ) {
			final double tw = targetWeight( t, friendly );
			if( tw > weight ) {
				weight = tw;
				target = t;
			}
		}
		if( target == null ) {
			return new NothingAction();
		}
		while( friendly.size() > 0 ) {
			Planet src = null;
			double dist = Double.MAX_VALUE;
			for( final Planet s : friendly ) {
				final double sd = Voyager.distance( s, target );
				if( sd < dist ) {
					dist = sd;
					src = s;
				}
			}
			if( src != null ) {
				final int spare_soldiers = src.population( Unit.Soldier ) - garrison_;
				if( spare_soldiers > 0 ) {
					final int[] pop = new int[Unit.values().length];
					pop[Unit.Soldier.ordinal()] = spare_soldiers;
					return new LaunchAction( src, target, pop );
				}
				else {
					friendly.remove( src );
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
		return "KillPolicy" + self_.id;
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
