/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager.policies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.domains.voyager.EntityType;
import edu.oregonstate.eecs.mcplan.domains.voyager.LaunchAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.NothingAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.Planet;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.domains.voyager.SetProductionAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.Voyager;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerEvent;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;
import edu.oregonstate.eecs.mcplan.util.CircularListIterator;
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
public class BalancedPolicy implements AnytimePolicy<VoyagerState, VoyagerEvent>
{
	private final Player player_;
	private final RandomGenerator rng_;
	private final double min_strength_ratio_;
	private final double max_strength_ratio_;
	private final double win_margin_;
	private final double combat_worker_ratio_ = 0.25;
	private final double reinforce_soldier_ratio_ = 0.5;
	
	private VoyagerState s_ = null;
	private long t_ = 0;
	
	public BalancedPolicy( final Player player, final RandomGenerator rng,
						   final double min_strength_ratio,
						   final double max_strength_ratio, final double win_margin )
	{
		player_ = player;
		rng_ = rng;
		min_strength_ratio_ = min_strength_ratio;
		max_strength_ratio_ = max_strength_ratio;
		win_margin_ = win_margin;
	}
	
	@Override
	public void setState( final VoyagerState s, final long t )
	{
		s_ = s;
		t_ = t;
	}
	
	private static ArrayList<Planet> playerPlanets( final VoyagerState s, final Player p )
	{
		final ArrayList<Planet> planets = new ArrayList<Planet>();
		for( final Planet planet : s.planets ) {
			if( planet.owner() == p ) {
				planets.add( planet );
			}
		}
		return planets;
	}
	
	private static int strength( final int[] population )
	{
		int capture_strength = 0;
		for( final EntityType type : EntityType.values() ) {
			capture_strength += population[type.ordinal()] * type.strength();
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
	public VoyagerEvent getAction()
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
				for( final EntityType type : EntityType.values() ) {
					friendly_strength += p.population( type ) * type.strength();
					friendly_pop[type.ordinal()] += p.population( type );
				}
				worker_balance += p.population( EntityType.Worker );
				worker_balance -= p.capacity;
			}
			else {
				nonfriendly_planets.add( p );
				if( p.owner() == player_.enemy() ) {
					for( final EntityType type : EntityType.values() ) {
						enemy_strength += p.population( type ) * type.strength();
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
				final EntityType next = p.nextProduced();
				final int investment = p.storedProduction()[next.ordinal()] + Voyager.production( p );
				if( next != null && next.cost() <= investment ) {
					final CircularListIterator<EntityType> prod_itr = p.productionIterator();
					if( too_weak ) {
						if( prod_itr.hasNext() && prod_itr.next() == EntityType.Worker ) {
							expecting_planets.add( p );
						}
					}
					else { // too_strong
						if( prod_itr.hasNext() && prod_itr.next() == EntityType.Soldier ) {
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
						return new SetProductionAction(	p, Arrays.asList( EntityType.Soldier ) );
					}
					else if( worker_balance < 0 ) {
						return new SetProductionAction( p, Arrays.asList( EntityType.Worker ) );
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
			final int w = p.population( EntityType.Worker );
			if( w > p.capacity ) {
				final int over = w - p.capacity;
				if( over > overfullness ) {
					overfullness = over;
					overfull = p;
				}
			}
			else if( w < p.capacity ) {
				final int under = p.capacity - w;
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
				final int[] launch_pop = new int[EntityType.values().length];
				launch_pop[EntityType.Worker.ordinal()] = Math.min( underfullness, overfullness );
				return new LaunchAction( overfull, underfull, launch_pop );
			}
			else if( fullest != null && !fullest.equals( underfull ) ) {
				final int[] launch_pop = new int[EntityType.values().length];
				launch_pop[EntityType.Worker.ordinal()] = 1;
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
		double nn_strength = strength( near_nonfriendly.population() );
		nn_strength += win_margin_ * enemy_strength;
		final int points_needed = Math.max( (int) Math.ceil( nn_strength ), 1 );
		final int[] nf_pop = Arrays.copyOf( near_friendly.population(), EntityType.values().length );
		nf_pop[EntityType.Worker.ordinal()]
			= (int) Math.floor( combat_worker_ratio_ * nf_pop[EntityType.Worker.ordinal()] );
		final int nf_strength = strength( nf_pop );
		final int spare_workers = Math.max( 0, nf_pop[EntityType.Worker.ordinal()] - 1 );
		final int spare_soldiers = Math.max( 0, nf_pop[EntityType.Soldier.ordinal()] - 1 );
		final int usable_workers = Math.min( near_nonfriendly.capacity, spare_workers );
		final int soldiers_needed = Math.max( 0, (int) Math.ceil(
			(points_needed - (usable_workers * EntityType.Worker.strength())) / (double) EntityType.Soldier.strength() ) );
		if( spare_soldiers >= soldiers_needed ) {
			final int[] launch_pop = new int[EntityType.values().length];
			launch_pop[EntityType.Soldier.ordinal()] = soldiers_needed;
			launch_pop[EntityType.Worker.ordinal()] = usable_workers;
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
				if( p.id != near_friendly.id && p.population( EntityType.Soldier ) > 2 ) {
					final int[] launch_pop = new int[EntityType.values().length];
					launch_pop[EntityType.Soldier.ordinal()]
						= (int) Math.floor( reinforce_soldier_ratio_ * p.population( EntityType.Soldier ) );
					return new LaunchAction( p, near_friendly, launch_pop );
				}
			}
		}
		
		// TODO: Maybe we should switch everyone to Soldier production here
		// if all planets are maxed.
		for( final Planet p : friendly_planets ) {
			final int w = p.population( EntityType.Worker );
			if( w >= p.capacity && p.nextProduced() == EntityType.Worker ) {
				return new SetProductionAction( p, Arrays.asList( EntityType.Soldier ) );
			}
		}
		
		return new NothingAction();
	}

	@Override
	public void actionResult( final VoyagerEvent a, final VoyagerState sprime, final double r )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName()
	{
		return "BalancedPolicy";
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
	public VoyagerEvent getAction( final long control )
	{
		return getAction();
	}

}
