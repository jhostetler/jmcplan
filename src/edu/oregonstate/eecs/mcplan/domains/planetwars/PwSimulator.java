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
package edu.oregonstate.eecs.mcplan.domains.planetwars;

import java.util.ArrayDeque;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class PwSimulator implements UndoSimulator<PwState, PwEvent>
{
//	public class GrowthEvent implements PwEvent
//	{
//		public final PwPlanet planet;
//		public final int[] change;
//		private boolean done = false;
//
//		public GrowthEvent( final PwPlanet planet, final int[] change )
//		{
//			this.planet = planet;
//			this.change = change;
//		}
//
//		@Override
//		public GrowthEvent create()
//		{
//			return new GrowthEvent( planet, change );
//		}
//
//		@Override
//		public void doAction( final PwState state )
//		{
//			assert( !done );
//			final PwPlayer owner = planet.owner();
//			for( final PwUnit u : game.units() ) {
//				planet.setPopulation( owner, u, planet.population( owner, u ) + change[u.id] );
//				assert( planet.population( owner, u ) >= 0 );
//				assert( planet.population( owner, u ) <= planet.capacity );
//			}
//			done = true;
//		}
//
//		@Override
//		public void undoAction( final PwState state )
//		{
//			assert( done );
//			final PwPlayer owner = planet.owner();
//			for( final PwUnit u : game.units() ) {
//				planet.setPopulation( owner, u, planet.population( owner, u ) - change[u.id] );
//				assert( planet.population( owner, u ) >= 0 );
//				assert( planet.population( owner, u ) <= planet.capacity );
//			}
//			done = false;
//		}
//
//		@Override
//		public boolean isDone()
//		{
//			return done;
//		}
//	}
	
	/**
	 * Applies production points toward a particular Unit on a particular
	 * Planet. This event never creates a new unit, even if there are enough
	 * production points to pay for the unit.
	 */
	private static class PartialProductionEvent extends PwEvent
	{
		private final PwPlanet planet;
		private final PwUnit type;
		private final int production;
		private boolean done = false;
		
		public PartialProductionEvent( final PwPlanet planet, final PwUnit type, final int production )
		{
			this.planet = planet;
			this.type = type;
			this.production = production;
		}
		
		@Override
		public void undoAction( final PwState s )
		{
			assert( done );
			planet.setStoredProduction( type, planet.storedProduction( type ) - production );
			done = false;
		}

		@Override
		public void doAction( final RandomGenerator rng, final PwState s )
		{
			assert( !done );
			planet.setStoredProduction( type, planet.storedProduction( type ) + production );
			done = true;
		}

		@Override
		public boolean isDone()
		{ return done; }

		@Override
		public PartialProductionEvent create()
		{ return new PartialProductionEvent( planet, type, production ); }
		
		@Override
		public String toString()
		{ return "PartialProductionEvent(planet = " + planet.id + ", production = " + production + ")"; }

		@Override
		public int hashCode()
		{
			return new HashCodeBuilder( 7, 11 )
				.append( planet ).append( type ).append( production ).toHashCode();
		}

		@Override
		public boolean equals( final Object obj )
		{
			if( !(obj instanceof PartialProductionEvent) ) {
				return false;
			}
			final PartialProductionEvent that = (PartialProductionEvent) obj;
			return planet == that.planet
				   && type == that.type
				   && production == that.production;
		}
	}
	
	/**
	 * Creates a new unit on a particular Planet. Does not affect stored
	 * production points.
	 */
	private static class EntityCreateEvent extends PwEvent
	{
		public final PwPlanet planet;
		public final PwUnit type;
		private boolean done = false;
		
		public EntityCreateEvent( final PwPlanet planet, final PwUnit type )
		{
			this.planet = planet;
			this.type = type;
		}

		@Override
		public void doAction( final RandomGenerator rng, final PwState s )
		{
			assert( !done );
			planet.incrementPopulation( planet.owner(), type );
			planet.setStoredProduction( type, 0 );
			done = true;
		}

		@Override
		public void undoAction( final PwState state )
		{
			assert( done );
			planet.decrementPopulation( planet.owner(), type );
			planet.setStoredProduction( type, type.cost );
			done = false;
		}
		
		@Override
		public EntityCreateEvent create()
		{ return new EntityCreateEvent( planet, type ); }

		@Override
		public boolean isDone()
		{ return done; }
		
		@Override
		public String toString()
		{ return "EntityCreateEvent(planet = " + planet.id + ", type = " + type + ")"; }

		@Override
		public int hashCode()
		{
			return new HashCodeBuilder( 3, 11 )
				.append( planet ).append( type ).toHashCode();
		}

		@Override
		public boolean equals( final Object obj )
		{
			if( !(obj instanceof EntityCreateEvent) ) {
				return false;
			}
			final EntityCreateEvent that = (EntityCreateEvent) obj;
			return planet == that.planet
				   && type == that.type;
		}
	}
	
	/**
	 * Advances all units on all Routes and decrements the 'setup' timers.
	 */
	public static class ClockEvent extends PwEvent
	{
		private final PwPlanet[] planets;
		private final PwRoute[] routes;
		private boolean done = false;
		
		public ClockEvent( final PwPlanet[] planets, final PwRoute[] routes )
		{
			this.planets = planets;
			this.routes = routes;
		}
		
		@Override
		public ClockEvent create()
		{
			return new ClockEvent( planets, routes );
		}
		
		@Override
		public void doAction( final RandomGenerator rng, final PwState s )
		{
			assert( !done );
			for( final PwPlanet p : planets ) {
				if( p.owner() != PwPlayer.Neutral && p.getSetup() > 0 ) {
					p.decrementSetup();
				}
			}
			for( final PwRoute route : routes ) {
				route.forward();
			}
			done = true;
		}

		@Override
		public boolean isDone()
		{
			return done;
		}

		@Override
		public void undoAction( final PwState s )
		{
			assert( done );
			for( final PwPlanet p : planets ) {
				if( p.owner() != PwPlayer.Neutral && p.getSetup() < p.setup_time ) {
					p.incrementSetup();
				}
			}
			for( final PwRoute route : routes ) {
				route.backward();
			}
			done = false;
		}

		@Override
		public int hashCode()
		{
			return ClockEvent.class.hashCode();
		}

		@Override
		public boolean equals( final Object obj )
		{
			return obj instanceof ClockEvent;
		}

		@Override
		public String toString()
		{
			return "RouteForwardEvent";
		}
	}
	
	/**
	 * Transfers all units with 0 arrival time from the specified Route to
	 * their destination Planet.
	 */
	private class ArrivalEvent extends PwEvent
	{
		private final PwRoute route;
		private boolean done = false;
		
		private final int[][] ab_arrivals = new int[PwPlayer.Ncompetitors][game.Nunits()];
		private final int[][] ba_arrivals = new int[PwPlayer.Ncompetitors][game.Nunits()];
		
		public ArrivalEvent( final PwRoute route )
		{
			this.route = route;
		}

		@Override
		public void doAction( final RandomGenerator rng, final PwState s )
		{
			assert( !done );
			for( final PwPlayer player : PwPlayer.competitors ) {
				for( final PwUnit unit : game.units() ) {
					final int ab = route.populationAB( 0, player, unit );
					route.b.incrementPopulation( player, unit, ab );
					ab_arrivals[player.id][unit.id] = ab;
					
					final int ba = route.populationBA( 0, player, unit );
					route.a.incrementPopulation( player, unit, ba );
					ba_arrivals[player.id][unit.id] = ba;
				}
			}
			done = true;
		}

		@Override
		public void undoAction( final PwState s )
		{
			assert( done );
			for( final PwPlayer player : PwPlayer.competitors ) {
				for( final PwUnit unit : game.units() ) {
					final int ab = ab_arrivals[player.id][unit.id];
					route.b.decrementPopulation( player, unit, ab );
					route.setPopulationAB( 0, player, unit, ab );
					
					final int ba = ba_arrivals[player.id][unit.id];
					route.a.decrementPopulation( player, unit, ba );
					route.setPopulationBA( 0, player, unit, ba );
				}
			}
			done = false;
		}
		
		@Override
		public ArrivalEvent create()
		{ return new ArrivalEvent( route ); }

		@Override
		public boolean isDone()
		{ return done; }
		
		@Override
		public String toString()
		{
			return "ArrivalEvent[" + route + "]";
		}

		@Override
		public int hashCode()
		{
			return 13 + 17*route.hashCode();
		}

		@Override
		public boolean equals( final Object obj )
		{
			if( !(obj instanceof ArrivalEvent) ) {
				return false;
			}
			final ArrivalEvent that = (ArrivalEvent) obj;
			return route == that.route;
		}
	}
	
	/**
	 * Represents a battle. Two armies fight when they are both at the same
	 * planet. If the defending army is destroyed, ownership changes and
	 * all stored production is lost. In the event of a tie, Neutral takes
	 * control.
	 */
	private class BattleEvent extends PwEvent
	{
		public final PwPlanet planet;
		
		private boolean done = false;
		private PwPlayer old_owner = null;
		private final int[][] old_population = new int[PwPlayer.Ncompetitors][game.Nunits()];
		private final int[] old_carry = new int[PwPlayer.Ncompetitors];
		private PwUnit old_production = null;
		private final int[] old_stored = new int[game.Nunits()];
		private int old_overflow = 0;
		private final int old_setup = 0;
		
		public BattleEvent( final PwPlanet planet )
		{
			this.planet = planet;
		}
		
		@Override
		public void undoAction( final PwState s )
		{
			assert( done );
			planet.setOwner( old_owner );
			for( final PwPlayer y : PwPlayer.competitors ) {
				for( int i = 0; i < game.Nunits(); ++i ) {
					planet.setPopulation( y, game.unit( i ), old_population[y.id][i] );
				}
				planet.setCarryDamage( y, old_carry[y.id] );
			}
			planet.setProduction( old_production );
			planet.setStoredProduction( old_stored );
			planet.setOverflowProduction( old_overflow );
			planet.setSetup( old_setup );
			done = false;
		}

		@Override
		public void doAction( final RandomGenerator rng, final PwState s )
		{
			assert( !done );
			old_owner = planet.owner();
			for( final PwPlayer player : PwPlayer.competitors ) {
				Fn.memcpy( old_population[player.id], planet.population( player ) );
				old_carry[player.id] = planet.carryDamage( player );
			}
			old_production = planet.nextProduced();
			Fn.memcpy( old_stored, planet.storedProduction(), planet.storedProduction().length );
			old_overflow = planet.overflowProduction();
			
			final int[] damage = game.damage( planet );
			final int dmin = damage[PwPlayer.Min.id] + planet.carryDamage( PwPlayer.Max );
			final int dmax = damage[PwPlayer.Max.id] + planet.carryDamage( PwPlayer.Min );
			final Pair<int[], Integer> smin = game.survivors( planet.population( PwPlayer.Min ), dmax );
			final Pair<int[], Integer> smax = game.survivors( planet.population( PwPlayer.Max ), dmin );
			
			planet.setPopulation( PwPlayer.Min, smin.first );
			planet.setPopulation( PwPlayer.Max, smax.first );
			
			final boolean rmin = planet.supply( PwPlayer.Min ) > 0;
			final boolean rmax = planet.supply( PwPlayer.Max ) > 0;
			if( rmin && rmax ) {
				planet.setCarryDamage( PwPlayer.Min, smin.second );
				planet.setCarryDamage( PwPlayer.Max, smax.second );
			}
			else {
				// Owner change?
				boolean change = false;
				if( rmin ) {
					if( old_owner != PwPlayer.Min ) {
						planet.setOwner( PwPlayer.Min );
						change = true;
					}
				}
				else if( rmax ) {
					if( old_owner != PwPlayer.Max ) {
						planet.setOwner( PwPlayer.Max );
						change = true;
					}
				}
				else {
					planet.setOwner( PwPlayer.Neutral );
					change = true;
				}
				
				// Clear planet status if owner changed
				if( change ) {
					planet.setStoredProduction( Fn.repeat( 0, game.Nunits() ) );
					planet.setProduction( game.defaultProduction() );
					planet.clearCarryDamage();
					planet.setOverflowProduction( 0 );
					planet.resetSetup();
				}
			}
			
			done = true;
		}

		@Override
		public boolean isDone()
		{ return done; }

		@Override
		public BattleEvent create()
		{ return new BattleEvent( planet ); }
		
		@Override
		public String toString()
		{ return "BattleEvent[" + planet + "]"; }

		@Override
		public int hashCode()
		{
			return 11 + 13*planet.hashCode();
		}

		@Override
		public boolean equals( final Object obj )
		{
			if( !(obj instanceof BattleEvent) ) {
				return false;
			}
			final BattleEvent that = (BattleEvent) obj;
			return planet == that.planet;
		}
	}
	
	/**
	 * Change of ownership *without* loss of production. Should only be used
	 * to change from Neutral -> not Neutral.
	 */
	private static class OwnerChangeEvent extends PwEvent
	{
		public final PwPlanet planet;
		public final PwPlayer new_owner;
		
		private PwPlayer old_owner = null;
		
		public OwnerChangeEvent( final PwPlanet p, final PwPlayer y )
		{
			planet = p;
			new_owner = y;
		}

		@Override
		public void doAction( final RandomGenerator rng, final PwState s )
		{
			assert( old_owner == null );
			old_owner = planet.owner();
			assert( old_owner == PwPlayer.Neutral );
			planet.setOwner( new_owner );
			
			planet.resetSetup();
		}

		@Override
		public void undoAction( final PwState s )
		{
			assert( old_owner != null );
			planet.setOwner( old_owner );
			old_owner = null;
		}
		
		@Override
		public OwnerChangeEvent create()
		{ return new OwnerChangeEvent( planet, new_owner ); }

		@Override
		public boolean isDone()
		{ return old_owner != null; }
		
		@Override
		public String toString()
		{
			return "OwnerChangeEvent[p=" + planet.id + ", y=" + new_owner.id + "]";
		}

		@Override
		public int hashCode()
		{
			return new HashCodeBuilder( 11, 17 )
				.append( planet ).append( new_owner ).toHashCode();
		}

		@Override
		public boolean equals( final Object obj )
		{
			if( !(obj instanceof OwnerChangeEvent) ) {
				return false;
			}
			final OwnerChangeEvent that = (OwnerChangeEvent) obj;
			return planet == that.planet && new_owner == that.new_owner;
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final PwGame game;
	private final PwState s;
	private int depth_ = 0;
	
	private final ArrayDeque<ArrayDeque<PwEvent>> event_history = new ArrayDeque<ArrayDeque<PwEvent>>();
	
	public PwSimulator( final PwGame game, final PwState initial_state )
	{
		this.game = game;
		s = initial_state;
	}

	@Override
	public int nagents()
	{
		return 2;
	}

	@Override
	public long horizon()
	{
		return game.T - s.t;
	}

	@Override
	public PwState state()
	{
		return s;
	}
	
	private void applyEvent( final PwEvent e )
	{
		e.doAction( s );
		event_history.peek().add( e );
	}
	
	private void applyProduction( final PwPlanet p )
	{
		int production = (int) ( (0.5 + game.rng.nextDouble())*p.production() );
		
		while( production > 0 ) {
			final PwUnit next = p.nextProduced();
			final int rem = next.cost - p.storedProduction( next );
			final int delta = Math.min( rem, production );
			production -= delta;
			if( rem - delta == 0 ) {
				if( rem < next.cost ) {
					// Unit was partially built last turn; need this event
					// to restore the partial build for undo.
					applyEvent( new PartialProductionEvent( p, next, rem ) );
				}
				if( s.supply( p.owner() ) < game.max_population ) {
					applyEvent( new EntityCreateEvent( p, next ) );
				}
				else {
					// Player is at population cap
					break;
				}
			}
			else {
				assert( production == 0 );
				applyEvent( new PartialProductionEvent( p, next, delta ) );
			}
		}
	}

	protected void advance()
	{
		for( int t = 0; t < game.epoch; ++t ) {
			// Event ordering:
			// 1. Production
			// 2. Units move
			// 3. Units arrive / Reinforcements added
			// 4. Battles
			
			// 1. Production
			for( int i = 0; i < s.planets.length; ++i ) {
				final PwPlanet p = s.planets[i];
				if( p.owner() != PwPlayer.Neutral && p.getSetup() == 0 ) {
					applyProduction( p );
				}
			}
			
			// 2. Units move
			applyEvent( new ClockEvent( s.planets, s.routes ) );
			
			// 3. Units arrive
			for( final PwRoute route : s.routes ) {
				applyEvent( new ArrivalEvent( route ) );
			}
			
			// 4. Battles
			for( final PwPlanet p : s.planets ) {
				final boolean min_present = p.supply( PwPlayer.Min ) > 0;
				final boolean max_present = p.supply( PwPlayer.Max ) > 0;
				if( min_present && max_present ) {
					applyEvent( new BattleEvent( p ) );
				}
				else if( p.owner() == PwPlayer.Neutral ) {
					if( min_present ) {
						applyEvent( new OwnerChangeEvent( p, PwPlayer.Min ) );
					}
					else if( max_present ) {
						applyEvent( new OwnerChangeEvent( p, PwPlayer.Max ) );
					}
				}
			}
		}
	}

	@Override
	public double[] reward()
	{
		final PwPlayer winner = s.winner();
		if( winner == PwPlayer.Min ) {
			return new double[] { 1, -1 };
		}
		else if( winner == PwPlayer.Max ) {
			return new double[] { -1, 1 };
		}
		else {
			return new double[] { 0, 0 };
		}
	}

	@Override
	public boolean isTerminalState()
	{
		return s.isTerminal();
	}

	@Override
	public void takeAction( final JointAction<PwEvent> a )
	{
		final ArrayDeque<PwEvent> frame = new ArrayDeque<PwEvent>();
		event_history.push( frame );
		
		for( final PwEvent e : a ) {
			applyEvent( e );
		}
		
		advance();
		s.t += 1;
		depth_ += frame.size();
	}
	
	@Override
	public void untakeLastAction()
	{
		final ArrayDeque<PwEvent> frame = event_history.pop();
		final int frame_size = frame.size();
		while( !frame.isEmpty() ) {
			final PwEvent e = frame.pop();
			e.undoAction( s );
		}
		s.t -= 1;
		depth_ -= frame_size;
	}

	@Override
	public long depth()
	{
		return depth_;
	}

	@Override
	public long t()
	{
		return s.t;
	}

	@Override
	public int[] turn()
	{
		return new int[] { 0, 1 };
	}

	@Override
	public String detailString()
	{
		return "PwSimulator";
	}
}
