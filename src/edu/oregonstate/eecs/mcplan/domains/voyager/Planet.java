/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.io.PrintStream;
import java.text.DecimalFormat;

import edu.oregonstate.eecs.mcplan.experiments.CsvEntry;


/**
 * @author jhostetler
 *
 */
public class Planet implements Comparable<Planet>, CsvEntry
{
	public static final class Builder
	{
		private int id_ = -1;
		private VoyagerHash hash_ = null;
		private int capacity_ = 0;
		private int x_ = 0;
		private int y_ = 0;
		private Player owner_ = null;
		
		/** We are trusting you to give planets sequential, non-negative IDs! */
		public Builder id( final int id ) { id_ = id; return this; }
		public Builder hash( final VoyagerHash h ) { hash_ = h; return this; }
		public Builder capacity( final int c ) { capacity_ = c; return this; }
		public Builder x( final int x ) { x_ = x; return this; }
		public Builder y( final int y ) { y_ = y; return this; }
		public Builder owner( final Player p ) { owner_ = p; return this; }
		
		public Planet finish() { return new Planet( id_, hash_, capacity_, x_, y_, owner_ ); }
	}
	
	public final int id;
	public final int capacity;
	public final int x;
	public final int y;
	
	private final String csv_static_;
	
	private final int[] population_ = new int[EntityType.values().length];
	private int total_population_ = 0;
	private Player owner_;
	private EntityType next_produced_ = EntityType.defaultProduction();
	private final int[] stored_production_ = new int[EntityType.values().length];
	
	private final VoyagerHash hash_;
	private long zobrist_hash_ = 0L;
	private final char[] repr_ = new char[14];
	private static final DecimalFormat pop_format = new DecimalFormat( "000" );
	private static final int radix_10 = 10;
	private static final int owner_idx = 0;
	private static final int production_idx = 1;
	private static final int population_idx = 2;
	private static final int population_stride = 3;
	private static final int stored_production_idx = population_idx + (EntityType.values().length * population_stride);
	private static final int stored_production_stride = 3;
	
	@Override
	public void writeEntry( final PrintStream out )
	{
		out.print( csv_static_ );
		out.print( ";p=[" );
		for( int i = 0; i < population_.length; ++i ) {
			if( i > 0 ) {
				out.print( ";" );
			}
			out.print( population_[i] );
		}
		out.print( "];o=" );
		out.print( owner_.ordinal() );
		out.print( ";np=" );
		out.print( next_produced_ );
		out.print( ";sp=[" );
		for( int i = 0; i < stored_production_.length; ++i ) {
			if( i > 0 ) {
				out.print( ";" );
			}
			out.print( stored_production_[i] );
		}
		out.print( "]]" );
	}
	
	private Planet( final int id, final VoyagerHash hash, final int capacity, final int x, final int y, final Player owner )
	{
		this.id = id;
		hash_ = hash;
		this.capacity = capacity;
		this.x = x;
		this.y = y;
		csv_static_ = "Planet[id=" + id + ";x=" + x + ";y=" + y + ";c=" + capacity;
		
		this.owner_ = owner;
		
		// Initialize hash values
		zobrist_hash_ ^= hash.hashOwner( this, owner );
		repr_[owner_idx] = Character.forDigit( owner.id, radix_10 );
		zobrist_hash_ ^= hash.hashProduction( this, nextProduced() );
		repr_[production_idx] = Character.forDigit( nextProduced().ordinal(), radix_10 );
		for( final EntityType type : EntityType.values() ) {
			final int pop = population( type );
			final int pop_start = population_idx + (type.ordinal() * population_stride);
			zobrist_hash_ ^= hash.hashPopulation( this, type, pop );
			setChars( repr_, pop_format.format( pop ), pop_start );
			final int stored = storedProduction( type );
			final int stored_start = stored_production_idx + (type.ordinal() * stored_production_stride);
			zobrist_hash_ ^= hash.hashStoredProduction( this, type, stored );
			setChars( repr_, pop_format.format( stored ), stored_start );
		}
	}
	
	private void setChars( final char[] a, final String s, final int idx )
	{
		for( int i = 0; i < s.length(); ++i ) {
			a[idx + i] = s.charAt( i );
		}
	}
	
	public EntityType nextProduced()
	{
		return next_produced_;
	}
	
	public void setStoredProduction( final EntityType type, final int p )
	{
		assert( p >= 0 );
		assert( p <= type.cost() );
		final long old_hash = hash_.hashStoredProduction( this, type, stored_production_[type.ordinal()] );
		zobrist_hash_ ^= old_hash;
		stored_production_[type.ordinal()] = p;
		final long new_hash = hash_.hashStoredProduction( this, type, p );
		zobrist_hash_ ^= new_hash;
		setChars( repr_, pop_format.format( p ),
				   stored_production_idx + (type.ordinal() * stored_production_stride) );
	}
	
	public void setStoredProduction( final int[] production )
	{
		for( int i = 0; i < stored_production_.length; ++i ) {
//			final int p = production[i];
//			assert( p >= 0 );
//			assert( p <= EntityType.values()[i].cost() );
//			stored_production_[i] = p;
			setStoredProduction( EntityType.values()[i], production[i] );
		}
	}
	
	public int[] storedProduction()
	{
		return stored_production_;
	}
	
	public int storedProduction( final EntityType type )
	{
		return stored_production_[type.ordinal()];
	}
	
	public int remainingProduction( final EntityType type )
	{
		return type.cost() - stored_production_[type.ordinal()];
	}
	
	public Planet setProduction( final EntityType type )
	{
		final long old_hash = hash_.hashProduction( this, nextProduced() );
		zobrist_hash_ ^= old_hash;
		next_produced_ = type;
		final long new_hash = hash_.hashProduction( this, type );
		zobrist_hash_ ^= new_hash;
		repr_[production_idx] = Character.forDigit( type.ordinal(), radix_10 );
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
//		total_population_ = 0;
		for( int i = 0; i < pop.length; ++i ) {
//			assert( pop[i] >= 0 );
//			population_[i] = pop[i];
//			total_population_ += pop[i];
			setPopulation( EntityType.values()[i], pop[i] );
		}
		return this;
	}
	
	public Planet setPopulation( final EntityType type, final int pop )
	{
		assert( pop >= 0 );
		final int i = type.ordinal();
		total_population_ -= population_[i];
		final long old_hash = hash_.hashPopulation( this, type, population_[i] );
		zobrist_hash_ ^= old_hash;
		population_[i] = pop;
		total_population_ += pop;
		final long new_hash = hash_.hashPopulation( this, type, pop );
		zobrist_hash_ ^= new_hash;
		setChars( repr_, pop_format.format( pop ), population_idx + (type.ordinal() * population_stride) );
		assert( population_[i] >= 0 );
		assert( total_population_ >= 0 );
		return this;
	}
	
	public void incrementPopulation( final EntityType type, final int p )
	{
//		assert( p >= 0 );
//		final int i = type.ordinal();
//		population_[i] += p;
//		total_population_ += p;
//		assert( population_[i] >= 0 );
//		assert( total_population_ >= 0 );
		setPopulation( type, population_[type.ordinal()] + p );
	}
	
	public void incrementPopulation( final EntityType type )
	{
		incrementPopulation( type, 1 );
	}
	
	public void decrementPopulation( final EntityType type, final int p )
	{
//		assert( p >= 0 );
//		final int i = type.ordinal();
//		population_[i] -= p;
//		total_population_ -= p;
//		assert( population_[i] >= 0 );
//		assert( total_population_ >= 0 );
		setPopulation( type, population_[type.ordinal()] - p );
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
		final long old_hash = hash_.hashOwner( this, owner_ );
		zobrist_hash_ ^= old_hash;
		owner_ = owner;
		final long new_hash = hash_.hashOwner( this, owner );
		zobrist_hash_ ^= new_hash;
		repr_[owner_idx] = Character.forDigit( owner.id, radix_10 );
		return this;
	}
	
	public long zobristHash()
	{
		return zobrist_hash_;
	}
	
	public String repr()
	{
		return new String( repr_ );
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
