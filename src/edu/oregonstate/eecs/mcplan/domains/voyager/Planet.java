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
	
	private final int[][] population_ = new int[Player.Ncompetitors][Unit.values().length];
	private final int[] total_population_ = new int[Player.Ncompetitors];
	private final int[] carry_damage_ = new int[Player.Ncompetitors];
	private Player owner_;
	private Unit next_produced_ = Unit.defaultProduction();
	private final int[] stored_production_ = new int[Unit.values().length];
	
	private final VoyagerHash hash_;
	private long zobrist_hash_ = 0L;
	private static final DecimalFormat pop_format = new DecimalFormat( "000" );
	private static final int radix_10 = 10;
	private static final int owner_idx = 0;
	private static final int production_idx = 1;
	private static final int Npopulation = Player.Ncompetitors * Unit.values().length;
	private static final int population_idx = 2;
	private static final int population_stride = 3;
	private static final int Ncarry_damage = Player.Ncompetitors;
	private static final int carry_damage_idx = population_idx + (Npopulation * population_stride);
	private static final int carry_damage_stride = 3;
	private static final int Nstored_production = Unit.values().length;
	private static final int stored_production_idx = carry_damage_idx + (Ncarry_damage * carry_damage_stride);
//		= population_idx + (Player.Ncompetitors * Unit.values().length * population_stride);
	private static final int stored_production_stride = 3;
	private final char[] repr_
		= new char[stored_production_idx + (Nstored_production * stored_production_stride)];
//		= new char[1 + 1 + (Unit.values().length * (Player.Ncompetitors * population_stride + stored_production_stride))];
	
	@Override
	public void writeEntry( final PrintStream out )
	{
		out.print( csv_static_ );
		for( int i = 0; i < population_.length; ++i ) {
			out.print( ";p" );
			out.print( i );
			out.print( "=[" );
			for( int j = 0; j < population_[i].length; ++j ) {
				if( j > 0 ) {
					out.print( ";" );
				}
				out.print( population_[i][j] );
			}
		}
		out.print( "];d=[" );
		for( int i = 0; i < carry_damage_.length; ++i ) {
			if( i > 0 ) {
				out.print( ";" );
			}
			out.print( carry_damage_[i] );
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
		for( final Player player : Player.competitors ) {
			for( final Unit type : Unit.values() ) {
				final int pop = population( player, type );
				final int pop_start = population_idx
									  + (player.ordinal() * Unit.values().length * population_stride)
									  + (type.ordinal() * population_stride);
				zobrist_hash_ ^= hash.hashPopulation( this, player, type, pop );
				setChars( repr_, pop_format.format( pop ), pop_start );
			}
		}
		for( final Player player : Player.competitors ) {
			final int dmg_start = carry_damage_idx + (player.ordinal() * carry_damage_stride);
			zobrist_hash_ ^= hash.hashCarryDamage( this, player, 0 );
			setChars( repr_, pop_format.format( 0 ), dmg_start );
		}
		for( final Unit type : Unit.values() ) {
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
	
	public Unit nextProduced()
	{
		return next_produced_;
	}
	
	public void setStoredProduction( final Unit type, final int p )
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
			setStoredProduction( Unit.values()[i], production[i] );
		}
	}
	
	public int[] storedProduction()
	{
		return stored_production_;
	}
	
	public int storedProduction( final Unit type )
	{
		return stored_production_[type.ordinal()];
	}
	
	public int remainingProduction( final Unit type )
	{
		return type.cost() - stored_production_[type.ordinal()];
	}
	
	public Planet setProduction( final Unit type )
	{
		final long old_hash = hash_.hashProduction( this, nextProduced() );
		zobrist_hash_ ^= old_hash;
		next_produced_ = type;
		final long new_hash = hash_.hashProduction( this, type );
		zobrist_hash_ ^= new_hash;
		repr_[production_idx] = Character.forDigit( type.ordinal(), radix_10 );
		return this;
	}
	
	public int population( final Player player, final Unit type )
	{
		return population_[player.ordinal()][type.ordinal()];
	}
	
	public int[] population( final Player player )
	{
		return population_[player.ordinal()];
	}
	
	public int[][] population()
	{
		return population_;
	}
	
	public int totalPopulation( final Player player )
	{
		return total_population_[player.ordinal()];
	}
	
	public Planet setPopulation( final Player player, final int[] pop )
	{
		assert( pop.length == population_.length );
//		total_population_ = 0;
		for( int i = 0; i < pop.length; ++i ) {
//			assert( pop[i] >= 0 );
//			population_[i] = pop[i];
//			total_population_ += pop[i];
			setPopulation( player, Unit.values()[i], pop[i] );
		}
		return this;
	}
	
	public Planet setPopulation( final Player player, final Unit type, final int pop )
	{
		assert( pop >= 0 );
		final int i = player.ordinal();
		final int j = type.ordinal();
		total_population_[i] -= population_[i][j];
		final long old_hash = hash_.hashPopulation( this, player, type, population_[i][j] );
		zobrist_hash_ ^= old_hash;
		population_[i][j] = pop;
		total_population_[i] += pop;
		final long new_hash = hash_.hashPopulation( this, player, type, pop );
		zobrist_hash_ ^= new_hash;
		final int idx = population_idx + (player.ordinal() * Unit.values().length * population_stride)
									   + (type.ordinal() * population_stride);
		setChars( repr_, pop_format.format( pop ), idx );
		assert( population_[i][j] >= 0 );
		assert( total_population_[i] >= 0 );
		return this;
	}
	
	public void incrementPopulation( final Player player, final Unit type, final int p )
	{
//		assert( p >= 0 );
//		final int i = type.ordinal();
//		population_[i] += p;
//		total_population_ += p;
//		assert( population_[i] >= 0 );
//		assert( total_population_ >= 0 );
		setPopulation( player, type, population_[player.ordinal()][type.ordinal()] + p );
	}
	
	public void incrementPopulation( final Player player, final Unit type )
	{
		incrementPopulation( player, type, 1 );
	}
	
	public void decrementPopulation( final Player player, final Unit type, final int p )
	{
//		assert( p >= 0 );
//		final int i = type.ordinal();
//		population_[i] -= p;
//		total_population_ -= p;
//		assert( population_[i] >= 0 );
//		assert( total_population_ >= 0 );
		setPopulation( player, type, population_[player.ordinal()][type.ordinal()] - p );
	}
	
	public void decrementPopulation( final Player player, final Unit type )
	{
		decrementPopulation( player, type, 1 );
	}
	
	public int[] carryDamage()
	{
		return carry_damage_;
	}
	
	public int carryDamage( final Player player )
	{
		return carry_damage_[player.id];
	}
	
	public Planet setCarryDamage( final Player player, final int dmg )
	{
		final long old_hash = hash_.hashCarryDamage( this, player, carry_damage_[player.id] );
		zobrist_hash_ ^= old_hash;
		carry_damage_[player.id] = dmg;
		final long new_hash = hash_.hashCarryDamage( this, player, dmg );
		zobrist_hash_ ^= new_hash;
		setChars( repr_, pop_format.format( dmg ), carry_damage_idx + (player.ordinal() * carry_damage_stride) );
		return this;
	}
	
	public Planet clearCarryDamage()
	{
		for( final Player y : Player.competitors ) {
			setCarryDamage( y, 0 );
		}
		return this;
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
	
	public boolean contested()
	{
		return totalPopulation( Player.Min ) > 0
			   && totalPopulation( Player.Max ) > 0;
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
