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
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.Arrays;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public final class VoyagerSimulator<A extends UndoableAction<VoyagerState> & VirtualConstructor<A>>
	extends SimultaneousMoveSimulator<VoyagerState, A>
{
	public static final Logger log = LoggerFactory.getLogger( VoyagerSimulator.class );
	
	/**
	 * Applies production points toward a particular EntityType on a particular
	 * Planet. This event never creates a new unit, even if there are enough
	 * production points to pay for the unit.
	 */
	private static class PartialProductionEvent extends VoyagerAction
	{
		private final Planet planet_;
		private final Unit type_;
		private final int production_;
		private boolean done_ = false;
		private final String repr_;
		
		public PartialProductionEvent( final Planet planet, final Unit type, final int production )
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
		public void doAction( final RandomGenerator rng, final VoyagerState s )
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
	private static class EntityCreateEvent extends VoyagerAction
	{
		public final Planet planet_;
		public final Unit type_;
		private boolean done_ = false;
		private final String repr_;
		
		public EntityCreateEvent( final Planet planet, final Unit type )
		{
			planet_ = planet;
			type_ = type;
			repr_ = "EntityCreateEvent(planet = " + planet.id + ", type = " + type + ")";
		}

		@Override
		public void doAction( final RandomGenerator rng, final VoyagerState s )
		{
//			log.debug( "do {}", toString() );
			assert( !done_ );
			planet_.incrementPopulation( planet_.owner(), type_ );
			planet_.setStoredProduction( type_, 0 );
			done_ = true;
		}

		@Override
		public void undoAction( final VoyagerState state )
		{
//			log.debug( "undo {}", toString() );
			assert( done_ );
			planet_.decrementPopulation( planet_.owner(), type_ );
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
	private static class SpaceshipForwardEvent extends VoyagerAction
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
		public void doAction( final RandomGenerator rng, final VoyagerState s )
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
	private static class SpaceshipArrivalEvent extends VoyagerAction
	{
		public final Spaceship spaceship_;
		
		public SpaceshipArrivalEvent( final Spaceship ship )
		{
			assert( ship != null );
			assert( ship.arrival_time == 0 );
			spaceship_ = ship;
		}

		@Override
		public void doAction( final RandomGenerator rng, final VoyagerState s )
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
	 * Represents the arrival of new forces at a planet.
	 */
	private static class ReinforceEvent extends VoyagerAction
	{
		public final Planet planet;
		public final Player player;
		public final int[] population_change;
		private boolean done_ = false;
		private final String repr_;
		
		public ReinforceEvent( final Planet planet, final Player player, final int[] population_change )
		{
			this.planet = planet;
			this.player = player;
			this.population_change = population_change;
			repr_ = "ReinforceEvent(p = " + planet.id + ", y = " + player.id
				    + ", population_change = " + Arrays.toString( population_change ) + ")";
		}

		@Override
		public void undoAction( final VoyagerState s )
		{
//			log.debug( "undo {}", toString() );
			assert( done_ );
			for( int i = 0; i < Unit.values().length; ++i ) {
				planet.decrementPopulation( player, Unit.values()[i], population_change[i] );
			}
			done_ = false;
		}

		@Override
		public void doAction( final RandomGenerator rng, final VoyagerState s )
		{
//			log.debug( "do {}", toString() );
			assert( !done_ );
			for( int i = 0; i < Unit.values().length; ++i ) {
				planet.incrementPopulation( player, Unit.values()[i], population_change[i] );
			}
			done_ = true;
		}

		@Override
		public boolean isDone()
		{ return done_; }

		@Override
		public ReinforceEvent create()
		{ return new ReinforceEvent( planet, player, population_change ); }
		
		@Override
		public String toString()
		{ return repr_; }
	}
	
	private static class OwnerChangeEvent extends VoyagerAction
	{
		public final Planet planet;
		public final Player new_owner;
		
		private Player old_owner_ = null;
		
		public OwnerChangeEvent( final Planet p, final Player y )
		{
			planet = p;
			new_owner = y;
		}

		@Override
		public void doAction( final RandomGenerator rng, final VoyagerState s )
		{
			assert( old_owner_ == null );
			old_owner_ = planet.owner();
			planet.setOwner( new_owner );
		}

		@Override
		public void undoAction( final VoyagerState s )
		{
			assert( old_owner_ != null );
			planet.setOwner( old_owner_ );
			old_owner_ = null;
		}
		
		@Override
		public OwnerChangeEvent create()
		{ return new OwnerChangeEvent( planet, new_owner ); }

		@Override
		public boolean isDone()
		{ return old_owner_ != null; }
		
		@Override
		public String toString()
		{
			return "ChangeOwnerEvent(p = " + planet.id + ", y = " + new_owner.id + ")";
		}
	}
	
	/**
	 * Represents a battle.
	 */
	private static class BattleEvent extends VoyagerAction
	{
		public final Planet planet;
		private final String repr_;
		
		private boolean done_ = false;
		private Player old_owner_ = null;
		private final int[][] old_pop_ = new int[Player.Ncompetitors][Unit.values().length];
		private final int[] old_carry_ = new int[Player.Ncompetitors];
		private Unit old_production_ = null;
		private final int[] old_stored_ = new int[Unit.values().length];
		
		public BattleEvent( final Planet planet )
		{
			this.planet = planet;
			repr_ = "BattleEvent(planet = " + planet.id + ")";
		}
		
		@Override
		public void undoAction( final VoyagerState s )
		{
//			log.debug( "undo {}", toString() );
			assert( done_ );
			planet.setOwner( old_owner_ );
			for( final Player y : Player.competitors ) {
				planet.setPopulation( y, old_pop_[y.id] );
				planet.setCarryDamage( y, old_carry_[y.id] );
			}
			planet.setProduction( old_production_ );
			planet.setStoredProduction( old_stored_ );
			done_ = false;
		}

		@Override
		public void doAction( final RandomGenerator rng, final VoyagerState s )
		{
//			log.debug( "do {}", toString() );
			assert( !done_ );
			old_owner_ = planet.owner();
			for( final Player player : Player.values() ) {
				Fn.memcpy( old_pop_[player.id], planet.population( player ), planet.population().length );
				old_carry_[player.id] = planet.carryDamage( player );
			}
			old_production_ = planet.nextProduced();
			Fn.memcpy( old_stored_, planet.storedProduction(), planet.storedProduction().length );
			
			final Pair<Integer, Integer> damage = Voyager.damage( planet );
			final int dmin = damage.first + planet.carryDamage( Player.Max );
			final int dmax = damage.second + planet.carryDamage( Player.Min );
			final Pair<int[], Integer> smin = Voyager.survivors( planet.population( Player.Min ), dmax, rng );
			final Pair<int[], Integer> smax = Voyager.survivors( planet.population( Player.Max ), dmin, rng );
			
			planet.setPopulation( Player.Min, smin.first );
			planet.setPopulation( Player.Max, smax.first );
			
			final boolean rmin = planet.totalPopulation( Player.Min ) > 0;
			final boolean rmax = planet.totalPopulation( Player.Max ) > 0;
			if( rmin && rmax ) {
				planet.setCarryDamage( Player.Min, smin.second );
				planet.setCarryDamage( Player.Max, smax.second );
			}
			else {
				// Owner change
				if( rmin ) {
					planet.setOwner( Player.Min );
				}
				else if( rmax ) {
					planet.setOwner( Player.Max );
				}
				else {
					planet.setOwner( Player.Neutral );
				}
				planet.setStoredProduction( Fn.repeat( 0, Unit.values().length ) );
				planet.setProduction( Unit.defaultProduction() );
				planet.clearCarryDamage();
			}
			
			done_ = true;
		}

		@Override
		public boolean isDone()
		{ return done_; }

		@Override
		public BattleEvent create()
		{ return new BattleEvent( planet ); }
		
		@Override
		public String toString()
		{ return repr_; }
	}
	
	/**
	 * Represents the simultaneous arrival of two opposing forces at a
	 * Neutral Planet. There is no defender advantage in this situation,
	 * hence "jump ball".
	 */
//	private static class JumpBallEvent extends VoyagerAction
//	{
//		public final Planet planet;
//		final int[][] jump;
//		private final RandomGenerator rng_;
//		private final String repr_;
//
//		private boolean done_ = false;
//
//		public JumpBallEvent( final Planet planet, final int[][] jump, final RandomGenerator rng )
//		{
//			this.planet = planet;
//			this.jump = jump;
//			rng_ = rng;
//			final StringBuilder sb = new StringBuilder( "JumpBallEvent(planet = " );
//			sb.append( planet.id ).append( ", [" )
//			  .append( Arrays.toString( jump[Player.Min.ordinal()] ) ).append( ", " )
//			  .append( Arrays.toString( jump[Player.Max.ordinal()] ) ).append( "]" );
//			repr_ = sb.toString();
//		}
//
//		@Override
//		public void undoAction( final VoyagerState s )
//		{
////			log.debug( "undo {}", toString() );
//			assert( done_ );
//			planet.setOwner( Player.Neutral );
//			planet.setPopulation( Fn.repeat( 0, Unit.values().length ) );
//			planet.setProduction( Unit.defaultProduction() );
//			planet.setStoredProduction( Fn.repeat( 0, Unit.values().length ) );
//			done_ = false;
//		}
//
//		@Override
//		public void doAction( final VoyagerState s )
//		{
////			log.info( "do {}", toString() );
//			assert( !done_ );
//			assert( planet.owner() == Player.Neutral );
//
//			final int[] totals = new int[Player.Ncompetitors];
//			final int[][] jump_cp = new int[Player.Ncompetitors][];
//			for( int i = 0; i < Player.Ncompetitors; ++i ) {
//				totals[i] = Fn.sum( jump[i] );
//				jump_cp[i] = Arrays.copyOf( jump[i], Unit.values().length );
//			}
//
//			final int min_strength = Voyager.attack_strength( jump_cp[Player.Min.ordinal()] );
//			final int max_strength = Voyager.attack_strength( jump_cp[Player.Max.ordinal()] );
//			final double p = Voyager.jumpProbability( min_strength, max_strength );
//			log.debug( "a = {}, d = {}, p = {}", min_strength, max_strength, p );
//
//			while( totals[Player.Min.ordinal()] > 0 && totals[Player.Max.ordinal()] > 0 ) {
//				final Pair<Unit, Unit> matchup
//					= Voyager.minimaxMatchup( jump_cp[Player.Min.ordinal()], jump_cp[Player.Max.ordinal()] );
//				final double sample = rng_.nextDouble();
//				if( sample < p ) {
//					jump_cp[Player.Max.ordinal()][matchup.second.ordinal()] -= 1;
//					totals[Player.Max.ordinal()] -= matchup.second.attack();
//				}
//				else {
//					jump_cp[Player.Min.ordinal()][matchup.first.ordinal()] -= 1;
//					totals[Player.Min.ordinal()] -= matchup.first.attack();
//				}
//			}
//
//			for( int i = 0; i < Player.Ncompetitors; ++i ) {
//				if( totals[i] != 0 ) {
//					planet.setOwner( Player.values()[i] );
//					planet.setPopulation( jump_cp[i] );
//					assert( Fn.sum( planet.storedProduction() ) == 0 );
//					assert( planet.nextProduced() == Unit.defaultProduction() );
//					break;
//				}
//			}
//			// If it was a draw, the conditional in the loop will never trigger.
//			assert( planet.totalPopulation() >= 0 );
//			done_ = true;
//		}
//
//		@Override
//		public boolean isDone()
//		{ return done_; }
//
//		@Override
//		public JumpBallEvent create()
//		{ return new JumpBallEvent( planet, jump, rng_ ); }
//
//		@Override
//		public String toString()
//		{ return repr_; }
//	}
	
	/**
	 * Changes the seed of the random number generator used by the simulator.
	 * This is used to implement deterministic simulations. Note that
	 * reseeding the RNG is a costly operation, so this event is mostly
	 * useful for debugging.
	 */
	private static class ReseedEvent extends VoyagerAction
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
		public void doAction( final RandomGenerator rng, final VoyagerState s )
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
	public int nagents()
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
		int production = Math.min( p.population( p.owner(), Unit.Worker ), p.capacity );
		while( production > 0 ) {
			final Unit next = p.nextProduced();
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
			// 3. Spaceships arrive / Reinforcements added
			// 4. Battles
			// 5. If not stochastic, change Rng seed.
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
//			final Map<Planet, int[]> reinforcements = new TreeMap<Planet, int[]>();
//			final Map<Player, Map<Planet, int[]>> attackers = new TreeMap<Player, Map<Planet, int[]>>();
//			final Map<Planet, int[][]> jump_balls = new HashMap<Planet, int[][]>();
//			for( final Player player : Player.values() ) {
//				attackers.put( player, new TreeMap<Planet, int[]>() );
//			}
			while( !s_.spaceships.isEmpty() && s_.spaceships.peek().arrival_time == 0 ) {
				final Spaceship ship = s_.spaceships.poll();
				final SpaceshipArrivalEvent arrival = new SpaceshipArrivalEvent( ship );
				applyEvent( arrival );
				applyEvent( new ReinforceEvent( ship.dest, ship.owner, ship.population ) );
//				int[] current = reinforcements.get( ship.dest );
//				if( current == null ) {
//					current = new int[Unit.values().length];
//					reinforcements.put( ship.dest, current );
//				}
//				for( int i = 0; i < current.length; ++i ) {
//					current[i] += ship.population[i];
//				}
			}
//			for( final Map.Entry<Planet, int[]> e : reinforcements.entrySet() ) {
//				applyEvent( new ReinforceEvent( e.getKey(), e.getValue() ) );
//			}
//			for( final Map.Entry<Planet, int[][]> e : jump_balls.entrySet() ) {
//				applyEvent( new JumpBallEvent( e.getKey(), e.getValue(), rng_ ) );
//			}
			for( final Planet p : s_.planets ) {
				final boolean min_present = p.totalPopulation( Player.Min ) > 0;
				final boolean max_present = p.totalPopulation( Player.Max ) > 0;
				if( min_present && max_present ) {
					applyEvent( new BattleEvent( p ) );
				}
				else if( p.owner() == Player.Neutral ) {
					if( min_present ) {
						applyEvent( new OwnerChangeEvent( p, Player.Min ) );
					}
					else if( max_present ) {
						applyEvent( new OwnerChangeEvent( p, Player.Max ) );
					}
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
	public double[] reward()
	{
		// TODO Auto-generated method stub
		return new double[nagents()];
	}

	@Override
	public boolean isTerminalState()
	{
		return t_ >= horizon_ || Voyager.winner( state() ) != null;
	}
}
