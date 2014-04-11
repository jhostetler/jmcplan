/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager.policies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.domains.voyager.LaunchAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.NothingAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.Planet;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.domains.voyager.SetProductionAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.Unit;
import edu.oregonstate.eecs.mcplan.domains.voyager.Voyager;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;
import edu.oregonstate.eecs.mcplan.util.GibbsDistribution;

/**
 * This policy is intended to be a good all-around policy.
 * 
 * Goals of the policy design:
 * 1) Build workers
 * 2) Ensure adequate defense
 * 3) Expand gradually
 * 
 * @author jhostetler
 */
public class BalancedPolicy extends AnytimePolicy<VoyagerState, VoyagerAction>
{
	// TODO: Seeding
	private static final MersenneTwister rng = new MersenneTwister( 42 );
	
	private final Player player_;
	private final RandomGenerator rng_;
	private final double min_strength_ratio_;
	private final double max_strength_ratio_;
	private final double win_margin_;
	private final String repr_;
	
	private final double combat_worker_ratio_ = 0.25;
	private final double reinforce_soldier_ratio_ = 0.5;
	
	private VoyagerState s_ = null;
	private long t_ = 0;
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder( 29, 43 )
			.append( player_ ).append( min_strength_ratio_ ).append( max_strength_ratio_ )
			.append( win_margin_ ).append( combat_worker_ratio_ ).append( reinforce_soldier_ratio_ ).toHashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
//		System.out.println( "BalancedPolicy.equals()" );
		if( obj == null || !(obj instanceof BalancedPolicy) ) {
			return false;
		}
		final BalancedPolicy that = (BalancedPolicy) obj;
//		System.out.println( "\t" + toString() + ".equals( " + that.toString() + " )" );
		return player_ == that.player_
			   && min_strength_ratio_ == that.min_strength_ratio_
			   && max_strength_ratio_ == that.max_strength_ratio_
			   && win_margin_ == that.win_margin_
			   && combat_worker_ratio_ == that.combat_worker_ratio_
			   && reinforce_soldier_ratio_ == that.reinforce_soldier_ratio_;
	}
	
	public BalancedPolicy( final Player player, final long seed,
						   final double min_strength_ratio,
						   final double max_strength_ratio, final double win_margin )
	{
		player_ = player;
		rng_ = rng;
		min_strength_ratio_ = min_strength_ratio;
		max_strength_ratio_ = max_strength_ratio;
		win_margin_ = win_margin;
		repr_ = "BalancedPolicy" + player.id + "[" + min_strength_ratio_ + ", " + max_strength_ratio_ + ", " + win_margin_ + "]";
	}
	
	public BalancedPolicy( final Player player, final long seed, final String[] params )
	{
		player_ = player;
		rng_ = new MersenneTwister( seed );
		assert( params.length == 3 );
		min_strength_ratio_ = Double.parseDouble( params[0] );
		max_strength_ratio_ = Double.parseDouble( params[1] );
		win_margin_ = Double.parseDouble( params[2] );
		repr_ = "BalancedPolicy[" + min_strength_ratio_ + ", " + max_strength_ratio_ + ", " + win_margin_ + "]";
	}
	
	@Override
	public void setState( final VoyagerState s, final long t )
	{
		s_ = s;
		t_ = t;
	}
	
	private static int defense( final int[] population )
	{
		int capture_strength = 0;
		for( final Unit type : Unit.values() ) {
			capture_strength += population[type.ordinal()] * type.hp();
		}
		return capture_strength;
	}

	/**
	 * The algorithm:
	 * 1. If any planets will produce a unit in the next time step, decide
	 *    whether we want to switch the production schedule.
	 * 2. If we didn't switch production, see if we want to launch an attack
	 *    on the nearest enemy planet.
	 * @see edu.oregonstate.eecs.mcplan.Policy#getAction()
	 */
	@Override
	public VoyagerAction getAction()
	{
		final ArrayList<Planet> friendly_planets = new ArrayList<Planet>();
		final ArrayList<Planet> nonfriendly_planets = new ArrayList<Planet>();
		final int[] friendly_pop = new int[] { 0, 0 };
		final int[] enemy_pop = new int[] { 0, 0 };
		int worker_balance = 0;
		int friendly_strength = 0;
		int enemy_strength = 0;
		for( final Planet p : s_.planets ) {
			if( p.owner() == player_ ) {
				friendly_planets.add( p );
				for( final Unit type : Unit.values() ) {
					friendly_strength += p.population( type ) * type.hp();
					friendly_pop[type.ordinal()] += p.population( type );
				}
				worker_balance += p.population( Unit.Worker );
				worker_balance -= p.capacity;
			}
			else {
				nonfriendly_planets.add( p );
				if( p.owner() == player_.enemy() ) {
					for( final Unit type : Unit.values() ) {
						enemy_strength += p.population( type ) * type.attack();
						enemy_pop[type.ordinal()] += p.population( type );
					}
				}
			}
		}
		// Consider production change actions
		final double strength_ratio = friendly_strength / (double) enemy_strength;
		final boolean too_weak = strength_ratio < min_strength_ratio_;
		final boolean too_strong = strength_ratio > max_strength_ratio_;
		if( too_weak || too_strong ) {
			final ArrayList<Planet> expecting_planets = new ArrayList<Planet>();
			for( final Planet p : friendly_planets ) {
				final Unit next = p.nextProduced();
				final int investment = p.storedProduction()[next.ordinal()];
				// Investment condition means planet completed production last turn
				if( next != null && investment < Voyager.production( p ) ) {
					if( too_weak ) {
						if( p.nextProduced() == Unit.Worker ) {
							expecting_planets.add( p );
						}
					}
					else { // too_strong
						if( p.nextProduced() == Unit.Soldier ) {
							expecting_planets.add( p );
						}
					}
				}
			}
			if( !expecting_planets.isEmpty() ) {
				Collections.sort( expecting_planets, new Comparator<Planet>() {
					@Override public int compare( final Planet a, final Planet b )
					{ return Voyager.production( b ) - Voyager.production( a ); }
				} );
				for( final Planet p : expecting_planets ) {
					if( too_weak ) {
						return new SetProductionAction(	p, Unit.Soldier );
					}
					else if( worker_balance < 0 ) {
						return new SetProductionAction( p, Unit.Worker );
					}
				}
			}
		}
		// No military launches; see if we want to shift workers
		Planet underfull = null;
		int underfullness = 0;
		Planet overfull = null;
		int overfullness = 0;
		Planet fullest = null;
		int fullness = 0;
		for( final Planet p : friendly_planets ) {
			final int w = p.population( Unit.Worker );
			final int weff = w + Voyager.enroute( s_, p, player_, Unit.Worker );
			if( w > p.capacity ) {
				final int over = w - p.capacity;
				if( over > overfullness ) {
					overfullness = over;
					overfull = p;
				}
			}
			else if( weff < p.capacity ) {
				final int under = p.capacity - weff;
				if( under > underfullness ) {
					underfullness = under;
					underfull = p;
				}
			}
			if( w > 1 && w > fullness ) {
				fullness = w;
				fullest = p;
			}
		}
		if( underfull != null ) {
			if( overfull != null ) {
				final int[] launch_pop = new int[Unit.values().length];
				launch_pop[Unit.Worker.ordinal()] = Math.min( underfullness, overfullness );
				return new LaunchAction( overfull, underfull, launch_pop );
			}
			else if( underfull.population( Unit.Worker ) == 0
					 && fullest != null && !fullest.equals( underfull ) ) {
				final int[] launch_pop = new int[Unit.values().length];
				launch_pop[Unit.Worker.ordinal()] = 1;
				return new LaunchAction( fullest, underfull, launch_pop );
			}
		}
		// Consider launch actions:
		// 1. Find the closest friendly-{enemy, neutral} pair.
		//    TODO: Need to make sure it's not already targeted
		// 2. If we can safely capture from the nearest planet, do it.
		// 3. If not, transfer some Soldiers from another planet to the
		//    friendly planet.
		// 4. If all else fails, see if we would like to re-balance workers.
		if( friendly_planets.size() == 0 || nonfriendly_planets.size() == 0 ) {
			return new NothingAction();
		}
		final GibbsDistribution distance_softmax = new GibbsDistribution( rng_ );
		for( final Planet f : friendly_planets ) {
			for( final Planet n : nonfriendly_planets ) {
				final double dist = Voyager.sq_distance( f, n );
				distance_softmax.add( -dist );
			}
		}
		final int fn_idx = distance_softmax.sample();
		final Planet near_friendly = friendly_planets.get( fn_idx / nonfriendly_planets.size() );
		final Planet near_nonfriendly = nonfriendly_planets.get( fn_idx % nonfriendly_planets.size() );
		double nn_strength = defense( near_nonfriendly.population() );
		nn_strength += win_margin_ * enemy_strength;
		final int points_needed = Math.max( (int) Math.ceil( nn_strength ), 1 );
		final int[] nf_pop = Arrays.copyOf( near_friendly.population(), Unit.values().length );
		nf_pop[Unit.Worker.ordinal()]
			= (int) Math.floor( combat_worker_ratio_ * nf_pop[Unit.Worker.ordinal()] );
		final int nf_strength = defense( nf_pop );
		final int spare_workers = Math.max( 0, nf_pop[Unit.Worker.ordinal()] - 1 );
		final int spare_soldiers = Math.max( 0, nf_pop[Unit.Soldier.ordinal()] - 1 );
		final int usable_workers = Math.min( near_nonfriendly.capacity, spare_workers );
		final int soldiers_needed = Math.max( 0, (int) Math.ceil(
			points_needed / (double) Unit.Soldier.attack() ) );
		if( spare_soldiers >= soldiers_needed ) {
			final int[] launch_pop = new int[Unit.values().length];
			launch_pop[Unit.Soldier.ordinal()] = soldiers_needed;
			launch_pop[Unit.Worker.ordinal()] = usable_workers;
			return new LaunchAction( near_friendly, near_nonfriendly, launch_pop );
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
		
		// TODO: Maybe we should switch everyone to Soldier production here
		// if all planets are maxed.
		for( final Planet p : friendly_planets ) {
			final int w = p.population( Unit.Worker );
			if( w >= p.capacity && p.nextProduced() == Unit.Worker ) {
				return new SetProductionAction( p, Unit.Soldier );
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
