/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;

import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class Spaceship
{
	public static class Factory
	{
		private int next_id_ = 0;
		public Spaceship create( final VoyagerHash hash, final Player owner, final int[] population,
										final Planet src, final Planet dest,
										final int x, final int y, final double speed )
		{
			return new Spaceship( next_id_++, hash, owner, population, src, dest, x, y, speed );
		}
	}
	
	public static final class Builder
	{
		private VoyagerHash hash_ = null;
		private Player owner_ = Player.Neutral;
		private final int[] population_ = new int[EntityType.values().length];
		private Planet src_ = null;
		private Planet dest_ = null;
		private int x_ = 0;
		private int y_ = 0;
		private double speed_ = 1.0;
		
		public Builder hash( final VoyagerHash h ) { hash_ = h; return this; }
		public Builder owner( final Player p ) { owner_ = p; return this; }
		public Builder population( final int[] p )
		{
			for( int i = 0; i < population_.length; ++i ) {
				population_[i] = p[i];
			}
			return this;
		}
		public Builder population( final EntityType type, final int p ) { population_[type.ordinal()] = p; return this; }
		public Builder src( final Planet p ) { src_ = p; return this; }
		public Builder dest( final Planet p ) { dest_ = p; return this; }
		public Builder x( final int x ) { x_ = x; return this; }
		public Builder y( final int y ) { y_ = y; return this; }
		public Builder speed( final double s ) { speed_ = s; return this; }
		
		public Spaceship finish( final Factory f )
		{ return f.create( hash_, owner_, population_, src_, dest_, x_, y_, speed_ ); }
	}
	
	/**
	 * The partial order for Spaceships. The ordering agrees with the order
	 * in which events related to spaceship arrival will be applied, in that
	 * reinforcements arrive before attackers if their arrival times are the
	 * same. Note that when calculating combat outcomes, all attackers are
	 * pooled, even if they are in different spaceships.
	 * 
	 * The ordering NEED NOT BE CONSISTENT ACROSS TIME STEPS, since the owner
	 * of a planet might change.
	 */
	public static final Comparator<Spaceship> ArrivalTimeComparator = new Comparator<Spaceship>()
	{
		@Override
		public int compare( final Spaceship a, final Spaceship b )
		{
			final int time_diff = a.arrival_time - b.arrival_time;
			if( time_diff != 0 ) {
				return time_diff;
			}
			else {
				if( a.dest.owner() == a.owner && b.dest.owner() != a.owner ) {
					return -1;
				}
				else if( a.dest.owner() != a.owner && b.dest.owner() == b.owner ) {
					return 1;
				}
				else {
					return 0;
				}
			}
		}
	};
	
	public final int id;
	public final Player owner;
	public final int[] population;
	public final Planet src;
	public final Planet dest;
	public double x = 0;
	public double y = 0;
	public final double speed;
	
	public final double velocity_x;
	public final double velocity_y;
	public int arrival_time = -1;
	
	private long zobrist_hash_ = 0L;
	private final VoyagerHash hash_;
	
	private final char[] repr_ = new char[2 + 2 + 2 + 1 + 1 + (EntityType.values().length * 3)];
	private static final DecimalFormat planet_format = new DecimalFormat( "00" );
	private static final DecimalFormat eta_format = new DecimalFormat( "00" );
	private static final DecimalFormat pop_format = new DecimalFormat( "000" );
	private static final int radix_10 = 10;
	private static final int src_idx = 0;
	private static final int dest_idx = 2;
	private static final int planet_stride = 2;
	private static final int eta_idx = 4;
	private static final int eta_stride = 2;
	private static final int owner_idx = 5;
	private static final int pop_idx = 6;
	private static final int pop_stride = 3;
	
	private Spaceship( final int id, final VoyagerHash hash, final Player owner, final int[] population,
					   final Planet src, final Planet dest, final int x, final int y, final double speed )
	{
		this.id = id;
		this.owner = owner;
		this.population = population;
		this.src = src;
		this.dest = dest;
		this.x = x;
		this.y = y;
		this.speed = speed;
		
		final double xdiff = dest.x - src.x;
		final double ydiff = dest.y - src.y;
		
		final double d = Math.sqrt( (xdiff * xdiff) + (ydiff * ydiff) );
		velocity_x = speed * (xdiff / d);
		velocity_y = speed * (ydiff / d);
		
		arrival_time = (int) Math.ceil( d / speed );
		
		hash_ = hash;
		setChars( repr_, planet_format.format( src.id ), src_idx );
		setChars( repr_, planet_format.format( dest.id ), dest_idx );
		setChars( repr_, eta_format.format( arrival_time ), eta_idx );
		repr_[owner_idx] = Character.forDigit( owner.id, radix_10 );
		for( final EntityType type : EntityType.values() ) {
			final int pop = population[type.ordinal()];
			zobrist_hash_ ^= hash_.hashSpaceship( src, dest, arrival_time, owner, type, pop );
			setChars( repr_, pop_format.format( pop ), pop_idx + (type.ordinal() * pop_stride) );
		}
	}
	
	private void setChars( final char[] a, final String s, final int idx )
	{
		for( int i = 0; i < s.length(); ++i ) {
			a[idx + i] = s.charAt( i );
		}
	}
	
	public int population()
	{
		return Fn.sum( population );
	}
	
	public void forward()
	{
		x += velocity_x;
		y += velocity_y;
		for( final EntityType type : EntityType.values() ) {
			zobrist_hash_ ^= hash_.hashSpaceship( src, dest, arrival_time, owner, type, population[type.ordinal()] );
		}
		arrival_time -= 1;
		for( final EntityType type : EntityType.values() ) {
			zobrist_hash_ ^= hash_.hashSpaceship( src, dest, arrival_time, owner, type, population[type.ordinal()] );
		}
		setChars( repr_, eta_format.format( arrival_time ), eta_idx );
	}
	
	public void backward()
	{
		x -= velocity_x;
		y -= velocity_y;
		for( final EntityType type : EntityType.values() ) {
			zobrist_hash_ ^= hash_.hashSpaceship( src, dest, arrival_time, owner, type, population[type.ordinal()] );
		}
		arrival_time += 1;
		for( final EntityType type : EntityType.values() ) {
			zobrist_hash_ ^= hash_.hashSpaceship( src, dest, arrival_time, owner, type, population[type.ordinal()] );
		}
		setChars( repr_, eta_format.format( arrival_time ), eta_idx );
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
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof Spaceship) ) {
			return false;
		}
		
		final Spaceship that = (Spaceship) obj;
		return id == that.id;
	}
	
	@Override
	public int hashCode()
	{
		return id;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "[Spaceship {id: " ).append( id )
		  .append(  "; src: " ).append( src.id )
		  .append( "; dest: " ).append( dest.id )
		  .append( "; player: " ).append( owner )
		  .append( "; x: " ).append( x ).append( "; y: " ).append( y )
		  .append( "; population: " ).append( Arrays.toString( population ) )
		  .append( "; eta: " ).append( arrival_time )
		  .append( "}]" );
		return sb.toString();
	}
}
