package edu.oregonstate.eecs.mcplan.domains.voyager.policies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.EntityType;
import edu.oregonstate.eecs.mcplan.domains.voyager.LaunchAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.NothingAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.Planet;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.domains.voyager.Voyager;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;
import edu.oregonstate.eecs.mcplan.util.Fn;

public class AttackPolicy implements AnytimePolicy<VoyagerState, UndoableAction<VoyagerState>>
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
		if( obj == null || !(obj instanceof AttackPolicy) ) {
			return false;
		}
		final AttackPolicy that = (AttackPolicy) obj;
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
	public UndoableAction<VoyagerState> getAction()
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
		final int[] nf_pop = Arrays.copyOf( near_friendly.population(), EntityType.values().length );
		nf_pop[EntityType.Worker.ordinal()]
			= (int) Math.floor( combat_worker_ratio_ * nf_pop[EntityType.Worker.ordinal()] );
		final int nf_strength = Voyager.defense_strength( nf_pop );
		final int spare_workers = Math.max( 0, nf_pop[EntityType.Worker.ordinal()] - 1 );
		final int spare_soldiers = Math.max( 0, nf_pop[EntityType.Soldier.ordinal()] - 1 );
		// FIXME: Sending workers is pointless since they now have 0 strength.
		// Fix it better!
		final int usable_workers = 0; //Math.min( target_.capacity, spare_workers );
		final int soldiers_needed = Math.max( 0, (int) Math.ceil(
			points_needed / (double) EntityType.Soldier.attack() ) );
		if( spare_soldiers >= soldiers_needed ) {
			final int[] launch_pop = new int[EntityType.values().length];
			launch_pop[EntityType.Soldier.ordinal()] = soldiers_needed;
			launch_pop[EntityType.Worker.ordinal()] = usable_workers;
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
				if( p.id != near_friendly.id && p.population( EntityType.Soldier ) > 2 ) {
					final int[] launch_pop = new int[EntityType.values().length];
					launch_pop[EntityType.Soldier.ordinal()]
						= (int) Math.floor( reinforce_soldier_ratio_ * p.population( EntityType.Soldier ) );
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
	public UndoableAction<VoyagerState> getAction( final long control )
	{
		return getAction();
	}

}
