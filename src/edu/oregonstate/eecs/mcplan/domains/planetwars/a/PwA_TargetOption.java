/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.planetwars.a;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.oregonstate.eecs.mcplan.Option;
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
public class PwA_TargetOption extends Option<PwState, PwEvent>
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
			
			final PwPlanet target = s.planets[target_id];
			
			// If a neighboring planet has population, attack from there
			int max_pop = 0;
			PwPlanet max_planet = null;
			for( final PwPlanet p : Fn.in( s.neighbors( target ) ) ) {
				if( p.owner() == player ) {
					if( p.supply( player ) > max_pop ) {
						max_pop = p.supply( player );
						max_planet = p;
					}
				}
			}
			if( max_planet != null ) {
				final int q = Fn.greatestLowerBound( PreferredNumbers.Series_1_2_5(), max_pop );
				if( q > 0 ) {
					return new PwLaunchAction( player, max_planet, s.route( max_planet, target ), new int[] { q } );
				}
			}
			
			// Otherwise, move some troops toward the target
			
			
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
	private final int target_id;
	
	private final Policy pi = new Policy();
	
	private int start_time = 0;
	// TODO: Make this a parameter
	private final int duration = 3;
	
	public PwA_TargetOption( final PwPlayer player, final int target_id )
	{
		this.player = player;
		this.target_id = target_id;
	}
	
	@Override
	public int hashCode()
	{
		final HashCodeBuilder hb = new HashCodeBuilder( 23, 17 );
		hb.append( player ).append( target_id );
		return hb.toHashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof PwA_TargetOption) ) {
			return false;
		}
		final PwA_TargetOption that = (PwA_TargetOption) obj;
		return player == that.player && target_id == that.target_id;
	}

	@Override
	public Option<PwState, PwEvent> create()
	{
		return new PwA_TargetOption( player, target_id );
	}

	@Override
	public void start( final PwState s, final long t )
	{
		start_time = s.t;
	}

	@Override
	public double terminate( final PwState s, final long t )
	{
		return (s.t - start_time) >= duration ? 1.0 : 0.0;
	}

	@Override
	public Policy pi()
	{
		return pi;
	}
}
