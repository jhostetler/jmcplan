/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.planetwars;

import java.text.DecimalFormat;

import edu.oregonstate.eecs.mcplan.util.Fn;



/**
 * @author jhostetler
 *
 */
public class PwPlanet implements Comparable<PwPlanet>
{
	private final PwGame game;
	
	public final int id;
	public final int capacity;
	private final int[][] population;
	public final int position_x;
	public final int position_y;
	
	private PwPlayer owner;
	
	private final int[] supply = new int[PwPlayer.Ncompetitors];
	private final int[] carry_damage = new int[PwPlayer.Ncompetitors];
	private PwUnit next_produced = null;
	private final int[] stored_production;
	private int overflow_production = 0;
	
	public final int setup_time = 5;
	private int setup = setup_time;
	
	private long zobrist_hash = 0L;
	
	private static final DecimalFormat pop_format = new DecimalFormat( "000" );
	private static final int radix_10 = 10;
	private static final int owner_idx = 0;
	private static final int production_idx = 1;
	private final int Npopulation;
	private static final int population_idx = 2;
	private static final int population_stride = 3;
	private static final int Ncarry_damage = PwPlayer.Ncompetitors;
	private final int carry_damage_idx;
	private static final int carry_damage_stride = 3;
	private final int Nstored_production;
	private final int stored_production_idx;
	private static final int stored_production_stride = 3;
	private final char[] repr;
	
	private final long[] owner_hash;
	private final long[][][] population_hash;
	private final long[] producing_hash;
	private final long[][] stored_production_hash;
	private final long[] overflow_production_hash;
	private final long[][] carry_damage_hash;
	private final long[] setup_hash;
	
	public PwPlanet( final PwPlanet that )
	{
		this.game = that.game;
		this.id = that.id;
		this.capacity = that.capacity;
		this.population = Fn.copy( that.population );
		this.position_x = that.position_x;
		this.position_y = that.position_y;
		this.owner = that.owner;
		Fn.memcpy( this.supply, that.supply );
		Fn.memcpy( this.carry_damage, that.carry_damage );
		this.next_produced = that.next_produced;
		this.stored_production = Fn.copy( that.stored_production );
		this.overflow_production = that.overflow_production;
		this.zobrist_hash = that.zobrist_hash;
		
		this.Npopulation = that.Npopulation;
		this.carry_damage_idx = that.carry_damage_idx;
		this.Nstored_production = that.Nstored_production;
		this.stored_production_idx = that.stored_production_idx;
		this.repr = that.repr;
		this.owner_hash = that.owner_hash;
		this.population_hash = that.population_hash;
		this.producing_hash = that.producing_hash;
		this.stored_production_hash = that.stored_production_hash;
		this.overflow_production_hash = that.overflow_production_hash;
		this.carry_damage_hash = that.carry_damage_hash;
		this.setup_hash = that.setup_hash;
	}

	public PwPlanet( final PwGame game, final int id, final int capacity, final int[][] population,
					 final int position_x, final int position_y, final PwPlayer owner )
	{
		this.game = game;
		this.id = id;
		this.capacity = capacity;
		this.population = population;
		this.position_x = position_x;
		this.position_y = position_y;
		this.owner = owner;
		
		Npopulation = PwPlayer.Ncompetitors * game.Nunits();
		Nstored_production = game.Nunits();
		carry_damage_idx = population_idx + (Npopulation * population_stride);
		stored_production_idx = carry_damage_idx + (Ncarry_damage * carry_damage_stride);
		repr = new char[stored_production_idx + (Nstored_production * stored_production_stride)];
		
		next_produced = game.defaultProduction();
		stored_production = new int[game.Nunits()];
		
		// Generate random numbers for Zobrist hashing
		owner_hash = new long[PwPlayer.Nplayers];
		for( int i = 0; i < PwPlayer.Nplayers; ++i ) {
			owner_hash[i] = game.rng.nextLong();
		}
		population_hash = new long[PwPlayer.Ncompetitors][game.Nunits()][]; // Need +1 for zero
		stored_production_hash = new long[game.Nunits()][];
		for( int t = 0; t < game.Nunits(); ++t ) {
			// +1 for zero
			final int max_supply = 1 + (game.max_population / game.unit( t ).supply);
			for( int player = 0; player < PwPlayer.Ncompetitors; ++player ) {
				population_hash[player][t] = new long[max_supply];
				for( int supply = 0; supply < max_supply; ++supply ) {
					population_hash[player][t][supply] = game.rng.nextLong();
				}
			}
			// FIXME: Didn't expect to need '+1' here; why doesn't
			// production happen immediately?
			stored_production_hash[t] = new long[game.unit( t ).cost + 1];
			for( int i = 0; i < stored_production_hash.length; ++i ) {
				stored_production_hash[t][i] = game.rng.nextLong();
			}
		}
		overflow_production_hash = new long[game.max_cost];
		for( int i = 0; i < overflow_production_hash.length; ++i ) {
			overflow_production_hash[i] = game.rng.nextLong();
		}
		producing_hash = new long[game.Nunits() + 1]; // Need one for 'null'
		for( int t = 0; t < producing_hash.length; ++t ) {
			producing_hash[t] = game.rng.nextLong();
		}
		carry_damage_hash = new long[PwPlayer.Ncompetitors][game.max_hp];
		for( int player = 0; player < PwPlayer.Ncompetitors; ++player ) {
			for( int hp = 0; hp < game.max_hp; ++hp ) {
				carry_damage_hash[player][hp] = game.rng.nextLong();
			}
		}
		setup_hash = new long[setup_time + 1];
		for( int t = 0; t < setup_hash.length; ++t ) {
			setup_hash[t] = game.rng.nextLong();
		}
		
		// Initialize hash values
		zobrist_hash ^= hashOwner( owner );
		repr[owner_idx] = Character.forDigit( owner.id, radix_10 );
		zobrist_hash ^= hashProducing( nextProduced() );
		repr[production_idx] = Character.forDigit( nextProduced().id, radix_10 );
		for( final PwPlayer player : PwPlayer.competitors ) {
			for( final PwUnit type : game.units() ) {
				final int pop = population( player, type );
				final int pop_start = population_idx
									  + (player.id * game.Nunits() * population_stride)
									  + (type.id * population_stride);
				zobrist_hash ^= hashPopulation( player, type, pop );
				setChars( repr, pop_format.format( pop ), pop_start );
			}
		}
		for( final PwPlayer player : PwPlayer.competitors ) {
			final int dmg_start = carry_damage_idx + (player.id * carry_damage_stride);
			zobrist_hash ^= hashCarryDamage( player, carry_damage[player.id] );
			setChars( repr, pop_format.format( carry_damage[player.id] ), dmg_start );
		}
		for( final PwUnit type : game.units() ) {
			final int stored = stored_production[type.id];
			final int stored_start = stored_production_idx + (type.id * stored_production_stride);
			zobrist_hash ^= hashStoredProduction( type, stored );
			setChars( repr, pop_format.format( stored ), stored_start );
		}
		zobrist_hash ^= hashOverflowProduction( overflow_production );
		zobrist_hash ^= hashSetup( setup );
	}
	
	private long hashOwner( final PwPlayer owner )
	{
		return owner_hash[owner.id];
	}
	
	private long hashPopulation( final PwPlayer y, final PwUnit type, final int population )
	{
		return population_hash[y.id][type.id][population];
	}
	
	private long hashCarryDamage( final PwPlayer y, final int damage )
	{
		return carry_damage_hash[y.id][damage];
	}
	
	private long hashProducing( final PwUnit type )
	{
		final int idx;
		if( type == null ) {
			idx = game.Nunits();
		}
		else {
			idx = type.id;
		}
		return producing_hash[idx];
	}
	
	private long hashStoredProduction( final PwUnit type, final int stored )
	{
		return stored_production_hash[type.id][stored];
	}
	
	private long hashOverflowProduction( final int production )
	{
		return overflow_production_hash[production];
	}
	
	private long hashSetup( final int setup )
	{
		return setup_hash[setup];
	}
	
	private void setChars( final char[] a, final String s, final int idx )
	{
		for( int i = 0; i < s.length(); ++i ) {
			a[idx + i] = s.charAt( i );
		}
	}
	
	// -----------------------------------------------------------------------
	
	
	/**
	 * FIXME: This will vary between variants of Planet Wars.
	 * @return
	 */
	public int production()
	{
		return 5;
	}
	
	public PwUnit nextProduced()
	{
		return next_produced;
	}
	
	public PwPlanet setStoredProduction( final PwUnit type, final int p )
	{
		assert( p >= 0 );
		assert( p <= type.cost );
		final long old_hash = hashStoredProduction( type, stored_production[type.id] );
		zobrist_hash ^= old_hash;
		stored_production[type.id] = p;
		final long new_hash = hashStoredProduction( type, p );
		zobrist_hash ^= new_hash;
		setChars( repr, pop_format.format( p ),
				   stored_production_idx + (type.id * stored_production_stride) );
		return this;
	}
	
	public PwPlanet setStoredProduction( final int[] production )
	{
		for( int i = 0; i < stored_production.length; ++i ) {
			setStoredProduction( game.unit( i ), production[i] );
		}
		return this;
	}
	
	public int[] storedProduction()
	{
		return stored_production;
	}
	
	public int storedProduction( final PwUnit type )
	{
		return stored_production[type.id];
	}
	
	public int remainingProduction( final PwUnit type )
	{
		return type.cost - stored_production[type.id];
	}
	
	public PwPlanet setOverflowProduction( final int production )
	{
		assert( production >= 0 );
		assert( production < game.max_cost );
		final long old_hash = hashOverflowProduction( overflow_production );
		zobrist_hash ^= old_hash;
		overflow_production = production;
		final long new_hash = hashOverflowProduction( overflow_production );
		zobrist_hash ^= new_hash;
		return this;
	}
	
	public int overflowProduction()
	{
		return overflow_production;
	}
	
	public PwPlanet setProduction( final PwUnit type )
	{
		final long old_hash = hashProducing( nextProduced() );
		zobrist_hash ^= old_hash;
		next_produced = type;
		final long new_hash = hashProducing( type );
		zobrist_hash ^= new_hash;
		repr[production_idx] = Character.forDigit( type.id, radix_10 );
		return this;
	}
	
	public int population( final PwPlayer player, final PwUnit type )
	{
		return population[player.id][type.id];
	}
	
	public int[] population( final PwPlayer player )
	{
		return population[player.id];
	}
	
	public int[][] population()
	{
		return population;
	}
	
	public int supply( final PwPlayer player )
	{
		return supply[player.id];
	}
	
	public PwPlanet setPopulation( final PwPlayer player, final int[] pop )
	{
		assert( pop.length == population( player ).length );
		for( int i = 0; i < pop.length; ++i ) {
			setPopulation( player, game.unit( i ), pop[i] );
		}
		return this;
	}
	
	public PwPlanet setPopulation( final PwPlayer player, final PwUnit type, final int pop )
	{
		assert( pop >= 0 );
		final int i = player.id;
		final int j = type.id;
//		supply[i] -= population[i][j]*type.supply;
		final long old_hash = hashPopulation( player, type, population[i][j] );
		zobrist_hash ^= old_hash;
		population[i][j] = pop;
		final long new_hash = hashPopulation( player, type, pop );
		zobrist_hash ^= new_hash;
		final int idx = population_idx + (player.ordinal() * game.Nunits() * population_stride)
									   + (type.id * population_stride);
		setChars( repr, pop_format.format( pop ), idx );
		
		supply[i] = pop*type.supply;
		assert( population[i][j] >= 0 );
		assert( supply[i] >= 0 );
		return this;
	}
	
	public PwPlanet incrementPopulation( final PwPlayer player, final PwUnit type, final int p )
	{
		return setPopulation( player, type, population[player.id][type.id] + p );
	}
	
	public PwPlanet incrementPopulation( final PwPlayer player, final PwUnit type )
	{
		return incrementPopulation( player, type, 1 );
	}
	
	public PwPlanet decrementPopulation( final PwPlayer player, final PwUnit type, final int p )
	{
		return setPopulation( player, type, population[player.id][type.id] - p );
	}
	
	public PwPlanet decrementPopulation( final PwPlayer player, final PwUnit type )
	{
		return decrementPopulation( player, type, 1 );
	}
	
	public int[] carryDamage()
	{
		return carry_damage;
	}
	
	public int carryDamage( final PwPlayer player )
	{
		return carry_damage[player.id];
	}
	
	public PwPlanet setCarryDamage( final PwPlayer player, final int dmg )
	{
		final long old_hash = hashCarryDamage( player, carry_damage[player.id] );
		zobrist_hash ^= old_hash;
		carry_damage[player.id] = dmg;
		final long new_hash = hashCarryDamage( player, dmg );
		zobrist_hash ^= new_hash;
		setChars( repr, pop_format.format( dmg ), carry_damage_idx + (player.ordinal() * carry_damage_stride) );
		return this;
	}
	
	public PwPlanet clearCarryDamage()
	{
		for( final PwPlayer y : PwPlayer.competitors ) {
			setCarryDamage( y, 0 );
		}
		return this;
	}
	
	public int getSetup()
	{
		return setup;
	}
	
	public PwPlanet setSetup( final int t )
	{
		final long old_hash = hashSetup( setup );
		zobrist_hash ^= old_hash;
		setup = t;
		final long new_hash = hashSetup( setup );
		zobrist_hash ^= new_hash;
		return this;
	}
	
	public PwPlanet incrementSetup()
	{
		return setSetup( setup + 1 );
	}
	
	public PwPlanet decrementSetup()
	{
		return setSetup( setup - 1 );
	}
	
	public PwPlanet resetSetup()
	{
		return setSetup( setup_time );
	}
	
	public PwPlayer owner()
	{
		return owner;
	}
	
	public PwPlanet setOwner( final PwPlayer new_owner )
	{
		final long old_hash = hashOwner( owner() );
		zobrist_hash ^= old_hash;
		this.owner = new_owner;
		final long new_hash = hashOwner( new_owner );
		zobrist_hash ^= new_hash;
		repr[owner_idx] = Character.forDigit( owner.id, radix_10 );
		return this;
	}
	
	public boolean contested()
	{
		return supply( PwPlayer.Min ) > 0
			   && supply( PwPlayer.Max ) > 0;
	}
	
	public long zobristHash()
	{
		return zobrist_hash;
	}
	
	public String repr()
	{
		return new String( repr );
	}
	
	@Override
	public int hashCode()
	{
		return 3 + 5*id;
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		final PwPlanet that = (PwPlanet) obj;
		return id == that.id;
	}
	
	@Override
	public int compareTo( final PwPlanet that )
	{
		return id - that.id;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "[PwPlanet {id: " ).append( id )
		  .append( "; x: " ).append( position_x ).append( "; y: " ).append( position_y )
		  .append( "; population: " ).append( population )
		  .append( "; capacity: " ).append( capacity )
		  .append( "; owner: " ).append( owner )
		  .append( "}]" );
		return sb.toString();
	}
}
