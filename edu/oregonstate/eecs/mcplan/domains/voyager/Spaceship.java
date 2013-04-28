/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.util.F;

/**
 * @author jhostetler
 *
 */
public class Spaceship
{
	private static int next_id_ = 0;
	public static Spaceship create( final Player owner, final int[] population,
									final Planet src, final Planet dest,
									final int x, final int y, final double speed )
	{
		return new Spaceship( next_id_++, owner, population, src, dest, x, y, speed );
	}
	
	public static final class Builder
	{
		private Player owner_ = Player.Neutral;
		private final int[] population_ = new int[EntityType.values().length];
		private Planet src_ = null;
		private Planet dest_ = null;
		private int x_ = 0;
		private int y_ = 0;
		private double speed_ = 1.0;
		
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
		
		public Spaceship finish() { return Spaceship.create( owner_, population_, src_, dest_, x_, y_, speed_ ); }
	}
	
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
	
	private Spaceship( final int id, final Player owner, final int[] population,
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
	}
	
	public int population()
	{
		return F.sum( population );
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
	
	public void forward()
	{
		x += velocity_x;
		y += velocity_y;
		arrival_time -= 1;
	}
	
	public void backward()
	{
		x -= velocity_x;
		y -= velocity_y;
		arrival_time += 1;
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
