/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public final class VoyagerSimulator<A extends UndoableAction<VoyagerState>>
	extends SimultaneousMoveSimulator<VoyagerState, A>
{
	public static final Logger log = LoggerFactory.getLogger( VoyagerSimulator.class );
	
	/**
	 * Applies production points toward a particular EntityType on a particular
	 * Planet. This event never creates a new unit, even if there are enough
	 * production points to pay for the unit.
	 */
	private static class PartialProductionEvent implements UndoableAction<VoyagerState>
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
	
	/**
	 * Creates a new unit on a particular Planet. Does not affect stored
	 * production points.
	 */
	private static class EntityCreateEvent implements UndoableAction<VoyagerState>
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
			done_ = true;
		}

		@Override
		public void undoAction( final VoyagerState state )
		{
//			log.debug( "undo {}", toString() );
			assert( done_ );
			planet_.decrementPopulation( type_ );
			planet_.setStoredProduction( type_, type_.cost() );
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
	
	/**
	 * Moves a Spaceship forward one time step.
	 */
	private static class SpaceshipForwardEvent implements UndoableAction<VoyagerState>
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
	
	/**
	 * Represents the arrival of a Spaceship. For implementation reasons, the
	 * 'doAction()' method doesn't actually do anything. However, you must
	 * still use SpaceshipArrivalEvents to get appropriate 'undo' behavior.
	 */
	private static class SpaceshipArrivalEvent implements UndoableAction<VoyagerState>
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
	
	/**
	 * Represents the arrival of forces to a Planet owned by the owner of the
	 * arriving forces.
	 */
	private static class ReinforceEvent implements UndoableAction<VoyagerState>
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
	
	/**
	 * Represents the arrival of forces to a Planet owned by an enemy of the
	 * owner of the forces.
	 */
	private static class BattleEvent implements UndoableAction<VoyagerState>
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
		private EntityType old_production_ = null;
		private int[] old_stored_ = null;
		
		public BattleEvent( final Planet planet, final Player attacker,
							final int[] population_change, final RandomGenerator rng )
		{
			this.planet = planet;
			this.attacker = attacker;
			this.population_change = population_change;
			total_change = Fn.sum( population_change );
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
			planet.setProduction( old_production_ );
			planet.setStoredProduction( old_stored_ );
			done_ = false;
		}

		@Override
		public void doAction( final VoyagerState s )
		{
//			log.debug( "do {}", toString() );
			assert( !done_ );
			old_owner_ = planet.owner();
			old_pop_ = Arrays.copyOf( planet.population(), planet.population().length );
			old_production_ = planet.nextProduced();
			old_stored_ = Arrays.copyOf( planet.storedProduction(), planet.storedProduction().length );
			
			final int[] attack_force = Arrays.copyOf( population_change, population_change.length );
			final int attack_strength = Voyager.attack_strength( attack_force );
			final int defense_strength = Voyager.defense_strength( planet.population() );
			int total = total_change;
			while( total > 0 && planet.totalPopulation() > 0 ) {
				final Pair<EntityType, EntityType> matchup
					= Voyager.minimaxMatchup( attack_force, planet.population() );
				final double sample = rng_.nextDouble();
				final double p = Voyager.winProbability( attack_strength, defense_strength );
//				log.info( "a = {}, d = {}, p = {}", attack_strength, defense_strength, p );
				if( sample < p ) {
					planet.decrementPopulation( matchup.second );
//					defense_strength -= matchup.second.defense();
				}
				else {
					attack_force[matchup.first.ordinal()] -= 1;
//					attack_strength -= matchup.first.attack();
					total -= 1;
				}
			}
			
			if( total > 0 ) {
				// Attacker won
				planet.setOwner( attacker );
				planet.setPopulation( attack_force );
				planet.setStoredProduction( Fn.repeat( 0, EntityType.values().length ) );
				// TODO: We're setting production to Worker by default. We
				// need to set it to *something*, but how do we give the
				// agent a choice?
				planet.setProduction( EntityType.defaultProduction() );
			}
			else if( planet.totalPopulation() == 0 ) {
				// A draw
				planet.setOwner( Player.Neutral );
				planet.setPopulation( new int[EntityType.values().length] );
				planet.setStoredProduction( Fn.repeat( 0, EntityType.values().length ) );
				planet.setProduction( EntityType.defaultProduction() );
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
	
	/**
	 * Represents the simultaneous arrival of two opposing forces at a
	 * Neutral Planet. There is no defender advantage in this situation,
	 * hence "jump ball".
	 */
	private static class JumpBallEvent implements UndoableAction<VoyagerState>
	{
		public final Planet planet;
		final int[][] jump;
		private final RandomGenerator rng_;
		private final String repr_;
		
		private boolean done_ = false;
		
		public JumpBallEvent( final Planet planet, final int[][] jump, final RandomGenerator rng )
		{
			this.planet = planet;
			this.jump = jump;
			rng_ = rng;
			final StringBuilder sb = new StringBuilder( "JumpBallEvent(planet = " );
			sb.append( planet.id ).append( ", [" )
			  .append( Arrays.toString( jump[Player.Min.ordinal()] ) ).append( ", " )
			  .append( Arrays.toString( jump[Player.Max.ordinal()] ) ).append( "]" );
			repr_ = sb.toString();
		}
		
		@Override
		public void undoAction( final VoyagerState s )
		{
//			log.debug( "undo {}", toString() );
			assert( done_ );
			planet.setOwner( Player.Neutral );
			planet.setPopulation( Fn.repeat( 0, EntityType.values().length ) );
			planet.setProduction( EntityType.defaultProduction() );
			planet.setStoredProduction( Fn.repeat( 0, EntityType.values().length ) );
			done_ = false;
		}

		@Override
		public void doAction( final VoyagerState s )
		{
//			log.info( "do {}", toString() );
			assert( !done_ );
			assert( planet.owner() == Player.Neutral );
			
			final int[] totals = new int[Player.competitors];
			final int[][] jump_cp = new int[Player.competitors][];
			for( int i = 0; i < Player.competitors; ++i ) {
				totals[i] = Fn.sum( jump[i] );
				jump_cp[i] = Arrays.copyOf( jump[i], EntityType.values().length );
			}
			
			final int min_strength = Voyager.attack_strength( jump_cp[Player.Min.ordinal()] );
			final int max_strength = Voyager.attack_strength( jump_cp[Player.Max.ordinal()] );
			final double p = Voyager.jumpProbability( min_strength, max_strength );
			log.debug( "a = {}, d = {}, p = {}", min_strength, max_strength, p );
			
			while( totals[Player.Min.ordinal()] > 0 && totals[Player.Max.ordinal()] > 0 ) {
				final Pair<EntityType, EntityType> matchup
					= Voyager.minimaxMatchup( jump_cp[Player.Min.ordinal()], jump_cp[Player.Max.ordinal()] );
				final double sample = rng_.nextDouble();
				if( sample < p ) {
					jump_cp[Player.Max.ordinal()][matchup.second.ordinal()] -= 1;
					totals[Player.Max.ordinal()] -= matchup.second.attack();
				}
				else {
					jump_cp[Player.Min.ordinal()][matchup.first.ordinal()] -= 1;
					totals[Player.Min.ordinal()] -= matchup.first.attack();
				}
			}
			
			for( int i = 0; i < Player.competitors; ++i ) {
				if( totals[i] != 0 ) {
					planet.setOwner( Player.values()[i] );
					planet.setPopulation( jump_cp[i] );
					assert( Fn.sum( planet.storedProduction() ) == 0 );
					assert( planet.nextProduced() == EntityType.defaultProduction() );
					break;
				}
			}
			// If it was a draw, the conditional in the loop will never trigger.
			assert( planet.totalPopulation() >= 0 );
			done_ = true;
		}

		@Override
		public boolean isDone()
		{ return done_; }

		@Override
		public JumpBallEvent create()
		{ return new JumpBallEvent( planet, jump, rng_ ); }
		
		@Override
		public String toString()
		{ return repr_; }
	}
	
	/**
	 * Changes the seed of the random number generator used by the simulator.
	 * This is used to implement deterministic simulations. Note that
	 * reseeding the RNG is a costly operation, so this event is mostly
	 * useful for debugging.
	 */
	private static class ReseedEvent implements UndoableAction<VoyagerState>
	{
		private final VoyagerSimulator sim_;
		private final long prev_seed_;
		private final long next_seed_;
		private final String repr_;
		private boolean done_ = false;
		
		public ReseedEvent( final VoyagerSimulator sim, final long prev_seed, final long next_seed )
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
	
	private final long initial_seed_;
	private final int max_population_;
	// These variables are managed by ReseedEvent
	private long prev_seed_ = 0;
	private long next_seed_ = 0;
	
	private final boolean stochastic_ = true;
	private final MersenneTwister rng_ = new MersenneTwister();
	private int t_ = 0;
	
	public VoyagerSimulator( final VoyagerState initial_state, final long seed, final int max_population,
							 final int horizon, final int epoch )
	{
		s_ = initial_state;
		initial_seed_ = seed;
		max_population_ = max_population;
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
	
	@Override
	public void setTurn( final int turn )
	{
		super.setTurn( turn );
		System.out.println( "[VoyagerSimulator] setTurn( " + turn + " )" );
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
				if( Voyager.playerPopulation( state(), p.owner() ) < max_population_ ) {
					applyEvent( new EntityCreateEvent( p, next ) );
				}
				else {
					log.debug( "! Player {} is maxed", p.owner() );
					break;
				}
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
			final Map<Planet, int[][]> jump_balls = new HashMap<Planet, int[][]>();
			for( final Player player : Player.values() ) {
				attackers.put( player, new TreeMap<Planet, int[]>() );
			}
			while( !s_.spaceships.isEmpty() && s_.spaceships.peek().arrival_time == 0 ) {
				final Spaceship ship = s_.spaceships.poll();
				final SpaceshipArrivalEvent arrival = new SpaceshipArrivalEvent( ship );
				applyEvent( arrival );
				if( ship.dest.owner() == ship.owner ) {
					int[] current = reinforcements.get( ship.dest );
					if( current == null ) {
						current = new int[EntityType.values().length];
						reinforcements.put( ship.dest, current );
					}
					for( int i = 0; i < current.length; ++i ) {
						current[i] += ship.population[i];
					}
				}
				else if( ship.dest.owner() == Player.Neutral ) {
					int[][] jump = jump_balls.get( ship.dest );
					if( jump == null ) {
						jump = new int[Player.competitors][];
						jump_balls.put( ship.dest, jump );
					}
					int[] player_jump = jump[ship.owner.ordinal()];
					if( player_jump == null ) {
						for( int i = 0; i < Player.competitors; ++i ) {
							jump[i] = new int[EntityType.values().length];
						}
						player_jump = jump[ship.owner.ordinal()];
					}
					Fn.vplus_inplace( player_jump, ship.population );
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
			for( final Map.Entry<Planet, int[][]> e : jump_balls.entrySet() ) {
				applyEvent( new JumpBallEvent( e.getKey(), e.getValue(), rng_ ) );
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
	public double[] getReward()
	{
		// TODO Auto-generated method stub
		return new double[getNumAgents()];
	}

	@Override
	public boolean isTerminalState()
	{
		return t_ >= horizon_ || Voyager.winner( state() ) != null;
	}
}
