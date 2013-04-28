/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;
import edu.oregonstate.eecs.mcplan.util.F;

/**
 * @author jhostetler
 *
 */
public final class VoyagerSimulator extends SimultaneousMoveSimulator<VoyagerState, VoyagerEvent>
{
	public static final Logger log = LoggerFactory.getLogger( VoyagerSimulator.class );
	
	public static class PartialProductionEvent extends VoyagerEvent
	{
		private final Planet planet_;
		private final EntityType type_;
		private final int production_;
		private boolean done_ = false;
		private final String repr_;
		
		public PartialProductionEvent( final Planet planet, final EntityType type, final int production )
		{
			planet_ = planet;
			type_ = type;
			production_ = production;
			repr_ = "PartialProductionEvent(planet = " + planet_.id + ", production = " + production_ + ")";
		}
		
		@Override
		public void undoAction( final VoyagerState s )
		{
//			log.debug( "undo {}", toString() );
			assert( done_ );
			planet_.setStoredProduction( type_, planet_.storedProduction()[type_.ordinal()] - production_ );
			done_ = false;
		}

		@Override
		public void doAction( final VoyagerState s )
		{
//			log.debug( "do {}", toString() );
			assert( !done_ );
			planet_.setStoredProduction( type_, planet_.storedProduction()[type_.ordinal()] + production_ );
			done_ = true;
		}

		@Override
		public boolean isDone()
		{ return done_; }

		@Override
		public PartialProductionEvent create()
		{ return new PartialProductionEvent( planet_, type_, production_ ); }
		
		@Override
		public String toString()
		{ return repr_; }
	}
	
	public static class EntityCreateEvent extends VoyagerEvent
	{
		public final Planet planet_;
		public final EntityType type_;
		private boolean done_ = false;
		private final String repr_;
		
		public EntityCreateEvent( final Planet planet, final EntityType type )
		{
			planet_ = planet;
			type_ = type;
			repr_ = "EntityCreateEvent(planet = " + planet.id + ", type = " + type + ")";
		}

		@Override
		public void doAction( final VoyagerState state )
		{
//			log.debug( "do {}", toString() );
			assert( !done_ );
			planet_.incrementPopulation( type_ );
			planet_.setStoredProduction( type_, 0 );
			planet_.productionForward();
			done_ = true;
		}

		@Override
		public void undoAction( final VoyagerState state )
		{
//			log.debug( "undo {}", toString() );
			assert( done_ );
			planet_.decrementPopulation( type_ );
			planet_.setStoredProduction( type_, type_.cost() );
			planet_.productionBackward();
			done_ = false;
		}
		
		@Override
		public EntityCreateEvent create()
		{ return new EntityCreateEvent( planet_, type_ ); }

		@Override
		public boolean isDone()
		{ return done_; }
		
		@Override
		public String toString()
		{ return repr_; }
	}
	
	public static class SpaceshipForwardEvent extends VoyagerEvent
	{
		public final Spaceship spaceship_;
		private boolean done_ = false;
		private final String repr_;
		
		public SpaceshipForwardEvent( final Spaceship spaceship )
		{
			spaceship_ = spaceship;
			repr_ = "SpaceshipForwardEvent(spaceship = " + spaceship.id + ")";
		}
		
		@Override
		public void doAction( final VoyagerState s )
		{
//			log.debug( "do {}", toString() );
			assert( !done_ );
			spaceship_.forward();
			done_ = true;
		}

		@Override
		public void undoAction( final VoyagerState s )
		{
//			log.debug( "undo {}", toString() );
			assert( done_ );
			spaceship_.backward();
			done_ = false;
		}
		
		@Override
		public SpaceshipForwardEvent create()
		{ return new SpaceshipForwardEvent( spaceship_ ); }
		
		@Override
		public boolean isDone()
		{ return done_; }
		
		@Override
		public String toString()
		{ return repr_; }
	}
	
	public static class SpaceshipArrivalEvent extends VoyagerEvent
	{
		public final Spaceship spaceship_;
		
		public SpaceshipArrivalEvent( final Spaceship ship )
		{
			assert( ship != null );
			assert( ship.arrival_time == 0 );
			spaceship_ = ship;
		}

		@Override
		public void doAction( final VoyagerState s )
		{
			assert( spaceship_ != null );
//			log.debug( "do {}", toString() );
			assert( spaceship_.arrival_time == 0 );
			assert( spaceship_.population() > 0 ); // TODO: Are 0 population spaceships allowed?
		}

		@Override
		public void undoAction( final VoyagerState s )
		{
			assert( spaceship_ != null );
//			log.debug( "undo {}", toString() );
			s.spaceships.add( spaceship_ );
		}
		
		@Override
		public SpaceshipArrivalEvent create()
		{ assert( false ); return new SpaceshipArrivalEvent( spaceship_ ); }

		@Override
		public boolean isDone()
		{ return spaceship_ != null; }
		
		@Override
		public String toString()
		{
			return "SpaceshipArrivalEvent(spaceship = " + (spaceship_ != null ? spaceship_.id : "null") + ")";
		}
	}
	
	public static class ReinforceEvent extends VoyagerEvent
	{
		public final Planet planet;
		public final int[] population_change;
		private boolean done_ = false;
		private final String repr_;
		
		public ReinforceEvent( final Planet planet, final int[] population_change )
		{
			this.planet = planet;
			this.population_change = population_change;
			repr_ = "ReinforceEvent(planet = " + planet.id
				    + ", population_change = " + Arrays.toString( population_change ) + ")";
		}

		@Override
		public void undoAction( final VoyagerState s )
		{
//			log.debug( "undo {}", toString() );
			assert( done_ );
			for( int i = 0; i < EntityType.values().length; ++i ) {
				planet.decrementPopulation( EntityType.values()[i], population_change[i] );
			}
			done_ = false;
		}

		@Override
		public void doAction( final VoyagerState s )
		{
//			log.debug( "do {}", toString() );
			assert( !done_ );
			for( int i = 0; i < EntityType.values().length; ++i ) {
				planet.incrementPopulation( EntityType.values()[i], population_change[i] );
			}
			done_ = true;
		}

		@Override
		public boolean isDone()
		{ return done_; }

		@Override
		public ReinforceEvent create()
		{ return new ReinforceEvent( planet, population_change ); }
		
		@Override
		public String toString()
		{ return repr_; }
	}
	
	public static class BattleEvent extends VoyagerEvent
	{
		public final Planet planet;
		public final Player attacker;
		public final int[] population_change;
		public final int total_change;
		private final RandomGenerator rng_;
		private final String repr_;
		
		private boolean done_ = false;
		private Player old_owner_ = null;
		private int[] old_pop_ = null;
		private List<EntityType> old_production_ = null;
		
		public BattleEvent( final Planet planet, final Player attacker,
							final int[] population_change, final RandomGenerator rng )
		{
			this.planet = planet;
			this.attacker = attacker;
			this.population_change = population_change;
			total_change = F.sum( population_change );
			rng_ = rng;
			repr_ = "BattleEvent(planet = " + planet.id
					+ ", population_change = " + Arrays.toString( population_change ) + ")";
		}
		
		@Override
		public void undoAction( final VoyagerState s )
		{
//			log.debug( "undo {}", toString() );
			assert( done_ );
			planet.setOwner( old_owner_ );
			planet.setPopulation( old_pop_ );
			planet.setProductionSchedule( old_production_ );
			done_ = false;
		}
		
		private Pair<EntityType, EntityType> minimaxMatchup( final int[] a, final int[] d )
		{
			double max_p = -Double.MAX_VALUE;
			int max_i = -1;
			int min_j = -1;
			for( int i = 0; i < a.length; ++i ) {
				if( a[i] == 0 ) {
					continue;
				}
				double min_p = Double.MAX_VALUE;
				for( int j = 0; j < d.length; ++j ) {
					if( d[j] == 0 ) {
						continue;
					}
					if( min_p > EntityType.matchups[i][j] ) {
						min_p = EntityType.matchups[i][j];
						min_j = j;
					}
				}
				if( min_p > max_p ) {
					max_p = min_p;
					max_i = i;
				}
			}
			assert( max_i >= 0 );
			assert( min_j >= 0 );
			return Pair.makePair( EntityType.values()[max_i], EntityType.values()[min_j] );
		}

		@Override
		public void doAction( final VoyagerState s )
		{
//			log.debug( "do {}", toString() );
			assert( !done_ );
			old_owner_ = planet.owner();
			old_pop_ = Arrays.copyOf( planet.population(), planet.population().length );
			old_production_ = planet.productionSchedule();
			
			final int[] attack_force = Arrays.copyOf( population_change, population_change.length );
			int total = total_change;
			while( total > 0 && planet.totalPopulation() > 0 ) {
				final Pair<EntityType, EntityType> matchup
					= minimaxMatchup( attack_force, planet.population() );
				final double sample = rng_.nextDouble();
				if( sample < EntityType.matchups[matchup.first.ordinal()][matchup.second.ordinal()] ) {
					planet.decrementPopulation( matchup.second );
				}
				else {
					attack_force[matchup.first.ordinal()] -= 1;
					total -= 1;
				}
			}
			
			if( total > 0 ) {
				// Attacker won
				planet.setOwner( attacker );
				planet.setPopulation( attack_force );
				// TODO: We're setting production to Worker by default. We
				// need to set it to *something*, but how do we give the
				// agent a choice?
				planet.setProductionSchedule( Arrays.asList( EntityType.Worker ) );
				planet.productionForward();
			}
			else if( planet.totalPopulation() == 0 ) {
				// A draw
				planet.setOwner( Player.Neutral );
				planet.setPopulation( new int[EntityType.values().length] );
			}
			
			assert( planet.totalPopulation() >= 0 );
			done_ = true;
		}

		@Override
		public boolean isDone()
		{ return done_; }

		@Override
		public BattleEvent create()
		{ return new BattleEvent( planet, attacker, population_change, rng_ ); }
		
		@Override
		public String toString()
		{ return repr_; }
	}
	
	private static class ReseedEvent extends VoyagerEvent
	{
		private final VoyagerSimulator sim_;
		private final int prev_seed_;
		private final int next_seed_;
		private final String repr_;
		private boolean done_ = false;
		
		public ReseedEvent( final VoyagerSimulator sim, final int prev_seed, final int next_seed )
		{
			sim_ = sim;
			prev_seed_ = prev_seed;
			next_seed_ = next_seed;
			repr_ = "ReseedEvent(prev_seed = " + prev_seed_ + ", next_seed = " + next_seed_ + ")";
		}
		
		@Override
		public void undoAction( final VoyagerState s )
		{
//			log.debug( "undo {}", toString() );
			assert( done_ );
			sim_.rng_.setSeed( prev_seed_ );
			sim_.prev_seed_ = prev_seed_;
			sim_.next_seed_ = next_seed_;
			done_ = false;
		}

		@Override
		public void doAction( final VoyagerState s )
		{
//			log.debug( "do {}", toString() );
			assert( !done_ );
			sim_.rng_.setSeed( next_seed_ );
			sim_.prev_seed_ = next_seed_;
			sim_.next_seed_ = sim_.rng_.nextInt();
			done_ = true;
		}

		@Override
		public boolean isDone()
		{ return done_; }

		@Override
		public ReseedEvent create()
		{ return new ReseedEvent( sim_, prev_seed_, next_seed_ ); }
		
		@Override
		public String toString()
		{ return repr_; }
	}
	
	// -----------------------------------------------------------------------
	
	private final VoyagerState s_;
	private final int epoch_;
	private final int horizon_;
	
	private final int initial_seed_;
	// These variables are managed by ReseedEvent
	private int prev_seed_ = 0;
	private int next_seed_ = 0;
	
	private final boolean stochastic_ = false;
	private final MersenneTwister rng_ = new MersenneTwister();
	private int t_ = 0;
	
	public VoyagerSimulator( final VoyagerState initial_state, final int seed, final int horizon, final int epoch )
	{
		s_ = initial_state;
		initial_seed_ = seed;
		prev_seed_ = initial_seed_;
		epoch_ = epoch;
		horizon_ = horizon / epoch;
	}

	@Override
	public int getNumAgents()
	{
		// TODO: generalize
		return 2;
	}

	@Override
	public long horizon()
	{
		return horizon_ - t_;
	}

	@Override
	public VoyagerState state()
	{
		return s_;
	}
	
	private void applyProduction( final Planet p )
	{
		int production = Math.min( p.population( EntityType.Worker ), p.capacity );
		while( production > 0 ) {
			final EntityType next = p.nextProduced();
			final int rem = next.cost() - p.storedProduction()[next.ordinal()];
			final int delta = Math.min( rem, production );
			production -= delta;
			if( rem - delta == 0 ) {
				if( rem < next.cost() ) {
					// Unit was partially built last turn; need this event
					// to restore the partial build for undo.
					applyEvent( new PartialProductionEvent( p, next, rem ) );
				}
				applyEvent( new EntityCreateEvent( p, next ) );
			}
			else {
				assert( production == 0 );
				applyEvent( new PartialProductionEvent( p, next, delta ) );
			}
		}
	}

	@Override
	protected void advance()
	{
		for( int t = 0; t < epoch_; ++t ) {
			// Event ordering:
			// 1. Growth
			// 2. Spaceships move
			// 3. Spaceships arrive
			// 4. Reinforcements added
			// 5. Attackers fight
			// 6. If not stochastic, change Rng seed.
			for( int i = 0; i < s_.planets.length; ++i ) {
				final Planet p = s_.planets[i];
				if( p.owner() != Player.Neutral ) {
					applyProduction( p );
				}
			}
			for( final Spaceship spaceship : s_.spaceships ) {
				final SpaceshipForwardEvent e = new SpaceshipForwardEvent( spaceship );
				applyEvent( e );
			}
			final Map<Planet, int[]> reinforcements = new TreeMap<Planet, int[]>();
			final Map<Player, Map<Planet, int[]>> attackers = new TreeMap<Player, Map<Planet, int[]>>();
			for( final Player player : Player.values() ) {
				attackers.put( player, new TreeMap<Planet, int[]>() );
			}
			while( !s_.spaceships.isEmpty() && s_.spaceships.peek().arrival_time == 0 ) {
				final Spaceship ship = s_.spaceships.poll();
				final SpaceshipArrivalEvent arrival = new SpaceshipArrivalEvent( ship );
				applyEvent( arrival );
				if( arrival.spaceship_.dest.owner() == arrival.spaceship_.owner ) {
					int[] current = reinforcements.get( arrival.spaceship_.dest );
					if( current == null ) {
						current = new int[EntityType.values().length];
						reinforcements.put( arrival.spaceship_.dest, current );
					}
					for( int i = 0; i < current.length; ++i ) {
						current[i] += arrival.spaceship_.population[i];
					}
				}
				else {
					int[] current = attackers.get( arrival.spaceship_.owner ).get( arrival.spaceship_.dest );
					if( current == null ) {
						current = new int[EntityType.values().length];
						attackers.get( arrival.spaceship_.owner ).put( arrival.spaceship_.dest, current );
					}
					for( int i = 0; i < current.length; ++i ) {
						current[i] += arrival.spaceship_.population[i];
					}
				}
			}
			for( final Map.Entry<Planet, int[]> e : reinforcements.entrySet() ) {
				applyEvent( new ReinforceEvent( e.getKey(), e.getValue() ) );
			}
			for( final Player player : Player.values() ) {
				for( final Map.Entry<Planet, int[]> e : attackers.get( player ).entrySet() ) {
					applyEvent( new BattleEvent( e.getKey(), player, e.getValue(), rng_ ) );
				}
			}
		}
		if( !stochastic_ ) {
			applyEvent( new ReseedEvent( this, prev_seed_, next_seed_ ) );
		}
		t_ += 1;
	}

	@Override
	protected void unadvance()
	{
		t_ -= 1;
	}

	@Override
	public double getReward()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isTerminalState( final VoyagerState s )
	{
		// TODO Auto-generated method stub
		return false;
	}
}
