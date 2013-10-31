/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager.policies;

import java.util.ArrayList;
import java.util.Arrays;

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
public class ExpansionPolicy extends Policy<VoyagerState, VoyagerAction>
{
	private final Player player_;
	private final int[] priority_list_;
	private final int garrison_;
	
	private VoyagerState s_ = null;
	private long t_ = 0L;
	
	public ExpansionPolicy( final Player player, final int[] priority_list, final int garrison )
	{
		player_ = player;
		priority_list_ = priority_list;
		garrison_ = garrison;
	}
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder( 761, 769 )
			.append( player_ ).append( Arrays.hashCode( priority_list_ ) ).append( garrison_ ).toHashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof ExpansionPolicy) ) {
			return false;
		}
		final ExpansionPolicy that = (ExpansionPolicy) obj;
		return player_ == that.player_
			   && Arrays.equals( priority_list_, that.priority_list_ )
			   && garrison_ == that.garrison_;
	}
	
	@Override
	public void setState( final VoyagerState s, final long t )
	{
		s_ = s;
		t_ = t;
	}
	
	private int[] unitsNeeded( final Planet p )
	{
		final int[] needed = new int[Unit.values().length];
		final int[] pop = Voyager.effectiveFriendlyPopulation( s_, p );
		needed[Unit.Worker.ordinal()] = Math.max( 0, p.capacity - pop[Unit.Worker.ordinal()] );
		needed[Unit.Soldier.ordinal()] = Math.max( 0, garrison_ - pop[Unit.Soldier.ordinal()] );
		return needed;
	}

	@Override
	public VoyagerAction getAction()
	{
		final ArrayList<Planet> friendly = Voyager.playerPlanets( s_, player_ );
		for( final int i : priority_list_ ) {
			final Planet p = s_.planets[i];
			if( p.owner() == player_.enemy() ) {
				continue; // This is ExpansionPolicy, so we don't try to attack
			}
			final int[] needed = unitsNeeded( p );
			if( needed[Unit.Worker.ordinal()] > 0 ) {
				// Find some workers
				for( final Planet q : friendly ) {
					if( !q.equals( p ) ) {
						final int surplus = q.population( Unit.Worker ) - q.capacity;
						if( surplus > 0 ) {
							final int to_send = Math.min( needed[Unit.Worker.ordinal()], surplus );
							return new LaunchAction( q, p, new int[] { to_send, 0 } );
						}
					}
				}
				// Didn't find workers, make sure we're producing them
				int producing_workers = 0;
				for( final Planet q : friendly ) {
					if( q.nextProduced() == Unit.Worker ) {
						producing_workers += 1;
					}
				}
				if( producing_workers == 0 ) {
					for( final Planet q : friendly ) {
						if( q.nextProduced() != Unit.Worker
							&& q.population( Unit.Worker ) > 0 ) {
							return new SetProductionAction( q, Unit.Worker );
						}
					}
				}
			}
			else if( needed[Unit.Soldier.ordinal()] > 0 ) {
				// Find some soldiers
				for( final Planet q : friendly ) {
					if( !q.equals( p ) ) {
						final int surplus = q.population( Unit.Soldier ) - garrison_;
						if( surplus > 0 ) {
							final int to_send = Math.min( needed[Unit.Soldier.ordinal()], surplus );
							return new LaunchAction( q, p, new int[] { 0, to_send } );
						}
					}
				}
				// Didn't find soldiers, make sure we're producing them
				int producing_soldiers = 0;
				for( final Planet q : friendly ) {
					if( q.nextProduced() == Unit.Soldier ) {
						producing_soldiers += 1;
					}
				}
				if( producing_soldiers == 0 ) {
					for( final Planet q : friendly ) {
						if( q.nextProduced() != Unit.Soldier
							&& q.population( Unit.Worker ) > 0 ) {
							return new SetProductionAction( q, Unit.Soldier );
						}
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
		return "ExpansionPolicy";
	}

}
