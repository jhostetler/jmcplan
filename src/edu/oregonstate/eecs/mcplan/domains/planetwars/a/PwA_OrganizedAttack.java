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

import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.oregonstate.eecs.mcplan.Option;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwEvent;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwPlanet;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwPlayer;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwRoute;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwState;

/**
 * @author jhostetler
 *
 */
public class PwA_OrganizedAttack extends Option<PwState, PwEvent>
{
	private class Policy extends edu.oregonstate.eecs.mcplan.Policy<PwState, PwEvent>
	{
		private PwState s = null;
		
		@Override
		public void setState( final PwState s, final long t )
		{
			this.s = s;
		}
	
		@Override
		public PwEvent getAction()
		{
			// 1. If there are enough units at the staging area, launch the
			//    attack
			// 2. Otherwise, find a good planet to send reinforcements.
			
			return null;
		}
	
		@Override
		public void actionResult( final PwState sprime, final double[] r )
		{
			// TODO Auto-generated method stub
			
		}
	
		@Override
		public String getName()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int hashCode()
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean equals( final Object that )
		{
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final PwPlayer player;
	private final PwPlanet target;
	private final double effort;
	
	private final Policy pi = new Policy();
	
	private int staging_id = -1;
	
	public PwA_OrganizedAttack( final PwPlayer player, final PwPlanet target, final double effort )
	{
		this.player = player;
		this.target = target;
		this.effort = effort;
	}
	
	@Override
	public int hashCode()
	{
		final HashCodeBuilder hb = new HashCodeBuilder( 23, 17 );
		hb.append( player ).append( target ).append( effort );
		return hb.toHashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof PwA_OrganizedAttack) ) {
			return false;
		}
		final PwA_OrganizedAttack that = (PwA_OrganizedAttack) obj;
		return player == that.player && target.equals( that.target ) && effort == that.effort;
	}

	@Override
	public Option<PwState, PwEvent> create()
	{
		return new PwA_OrganizedAttack( player, target, effort );
	}

	@Override
	public void start( final PwState s, final long t )
	{
		int min_d = Integer.MAX_VALUE;
		for( final PwPlanet p : s.planets ) {
			if( p.owner() == player ) {
				final PwRoute r = s.route( p, target );
				if( r != null ) {
					if( r.length < min_d ) {
						min_d = r.length;
						staging_id = p.id;
					}
				}
			}
		}
	}

	@Override
	public double terminate( final PwState s, final long t )
	{
		if( target.owner() == player ) {
			return 1.0;
		}
		if( staging_id == -1 ) {
			return 1.0;
		}
		
		// player lost control of staging planet
		final PwPlanet staging = s.planets[staging_id];
		if( staging.owner() != player ) {
			return 1.0;
		}
		
		// player has sent an attack force to target
		final PwRoute r = s.route( staging, target );
		if( (r.a.equals( staging ) && r.occupiedAB( player ))
				|| (r.b.equals( staging ) && r.occupiedBA( player )) ) {
			return 1.0;
		}
		
		// continue;
		return 0.0;
	}

	@Override
	public Policy pi()
	{
		return pi;
	}
}
