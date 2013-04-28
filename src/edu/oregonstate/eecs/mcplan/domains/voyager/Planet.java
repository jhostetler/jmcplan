/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.ArrayList;
import java.util.List;

import edu.oregonstate.eecs.mcplan.util.CircularListIterator;

/**
 * @author jhostetler
 *
 */
public class Planet implements Comparable<Planet>
{
	private static int next_id_ = 0;
	private static Planet create( final int capacity, final int x, final int y, final Player owner )
	{
		return new Planet( next_id_++, capacity, x, y, owner );
	}
	
	public static final class Builder
	{
		private int capacity_ = 0;
		private int x_ = 0;
		private int y_ = 0;
		private Player owner_ = null;
		
		public Builder capacity( final int c ) { capacity_ = c; return this; }
		public Builder x( final int x ) { x_ = x; return this; }
		public Builder y( final int y ) { y_ = y; return this; }
		public Builder owner( final Player p ) { owner_ = p; return this; }
		
		public Planet finish() { return Planet.create( capacity_, x_, y_, owner_ ); }
	}
	
	public final int id;
	public final int capacity;
	public final int x;
	public final int y;
	
	private final int[] population_ = new int[EntityType.values().length];
	private int total_population_ = 0;
	private Player owner_;
	private List<EntityType> production_schedule_ = new ArrayList<EntityType>();
	private CircularListIterator<EntityType> production_itr_ = null;
	private EntityType next_produced_ = null;
	private final int[] stored_production_ = new int[EntityType.values().length];
	
	private Planet( final int id, final int capacity, final int x, final int y, final Player owner )
	{
		this.id = id;
		this.capacity = capacity;
		this.x = x;
		this.y = y;
		this.owner_ = owner;
	}
	
	public EntityType nextProduced()
	{
		return next_produced_;
	}
	
	public void setStoredProduction( final EntityType type, final int p )
	{
		assert( p >= 0 );
		assert( p <= type.cost() );
		stored_production_[type.ordinal()] = p;
	}
	
	public void setStoredProduction( final int[] production )
	{
		for( int i = 0; i < stored_production_.length; ++i ) {
			final int p = production[i];
			assert( p >= 0 );
			assert( p <= EntityType.values()[i].cost() );
			stored_production_[i] = p;
		}
	}
	
	public int[] storedProduction()
	{
		return stored_production_;
	}
	
	public int remainingProduction( final EntityType type )
	{
		return type.cost() - stored_production_[type.ordinal()];
	}
	
	// TODO: Philosophically, forward and backward implement dynamics, so
	// they belong in Simulator. However, we'd still need to expose a
	// "next type" and "previous type" method for the iterator. Come to think
	// of it, those are dynamics too.
	public void productionForward()
	{
		next_produced_ = production_itr_.next();
	}
	
	public void productionBackward()
	{
		next_produced_ = production_itr_.previous();
	}
	
	/**
	 * Returns a copy of the iterator over the current production schedule.
	 * The next() method will return the element that comes *after* the
	 * nextProduced() element in the schedule.
	 * @return
	 */
	public CircularListIterator<EntityType> productionIterator()
	{
		return new CircularListIterator<EntityType>( production_itr_ );
	}
	
	public List<EntityType> productionSchedule()
	{
		return production_schedule_;
	}
	
	/**
	 * Sets a new production schedule. The planet will repeatedly produce
	 * entities in the order listed. The entity currently under production
	 * will be finished before beginning the new schedule.
	 * 
	 * @param schedule
	 * @return
	 */
	public Planet setProductionSchedule( final List<EntityType> schedule )
	{
		production_schedule_ = schedule;
		production_itr_ = new CircularListIterator<EntityType>( production_schedule_ );
		return this;
	}
	
	public int population( final EntityType type )
	{
		return population_[type.ordinal()];
	}
	
	public int[] population()
	{
		return population_;
	}
	
	public int totalPopulation()
	{
		return total_population_;
	}
	
	public Planet setPopulation( final int[] pop )
	{
		assert( pop.length == population_.length );
		total_population_ = 0;
		for( int i = 0; i < pop.length; ++i ) {
			assert( pop[i] >= 0 );
			population_[i] = pop[i];
			total_population_ += pop[i];
		}
		return this;
	}
	
	public Planet setPopulation( final EntityType type, final int pop )
	{
		assert( pop >= 0 );
		final int i = type.ordinal();
		total_population_ -= population_[i];
		population_[i] = pop;
		total_population_ += pop;
		assert( population_[i] >= 0 );
		assert( total_population_ >= 0 );
		return this;
	}
	
	public void incrementPopulation( final EntityType type, final int p )
	{
		assert( p >= 0 );
		final int i = type.ordinal();
		population_[i] += p;
		total_population_ += p;
		assert( population_[i] >= 0 );
		assert( total_population_ >= 0 );
	}
	
	public void incrementPopulation( final EntityType type )
	{
		incrementPopulation( type, 1 );
	}
	
	public void decrementPopulation( final EntityType type, final int p )
	{
		assert( p >= 0 );
		final int i = type.ordinal();
		population_[i] -= p;
		total_population_ -= p;
		assert( population_[i] >= 0 );
		assert( total_population_ >= 0 );
	}
	
	public void decrementPopulation( final EntityType type )
	{
		decrementPopulation( type, 1 );
	}
	
	public Player owner()
	{
		return owner_;
	}
	
	public Planet setOwner( final Player owner )
	{
		owner_ = owner;
		return this;
	}
	
	@Override
	public int hashCode()
	{
		return id;
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof Planet) ) {
			return false;
		}
		final Planet that = (Planet) obj;
		return id == that.id;
	}
	
	@Override
	public int compareTo( final Planet that )
	{
		return id - that.id;
	}
	
	@Override
	public String toString()
	{
		return "Planet" + id;
	}
}
