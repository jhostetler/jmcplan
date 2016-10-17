/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.fuelworld;

import org.apache.commons.math3.distribution.AbstractIntegerDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public class MoveAction extends FuelWorldAction
{
	private int old_location_ = -1;
	private int old_fuel_ = -1;
	private final boolean old_out_of_fuel_ = false;
	
	public final int src;
	public final int dest;
	
	private boolean done_ = false;
	
	private final int hash_code_;
	
	public MoveAction( final int src, final int dest )
	{
		this.src = src;
		this.dest = dest;
		
		hash_code_ = 3 + 11 * (src + 13 * dest);
	}
	
	@Override
	public void undoAction( final FuelWorldState s )
	{
		assert( done_ );
		s.location = old_location_;
		s.fuel = old_fuel_;
		s.out_of_fuel = old_out_of_fuel_;
		done_ = false;
	}
	
	public final AbstractIntegerDistribution getFuelConsumption( final FuelWorldState s )
	{
		return new UniformIntegerDistribution( s.rng, 1, s.fuel_consumption );
	}

	@Override
	public void doAction( final RandomGenerator rng, final FuelWorldState s )
	{
		assert( !done_ );
		assert( s.location == src );
		assert( s.adjacency.get( s.location ).contains( dest ) );
		
		old_location_ = s.location;
		old_fuel_ = s.fuel;
		
		final int consumption = getFuelConsumption( s ).sample(); // s.rng.nextInt( s.fuel_consumption );
		if( consumption > s.fuel ) {
			s.fuel = 0;
			s.out_of_fuel = true;
		}
		else {
			s.location = dest;
			s.fuel -= consumption;
		}
		
		done_ = true;
	}

	@Override
	public boolean isDone()
	{
		return done_;
	}

	@Override
	public FuelWorldAction create()
	{
		return new MoveAction( src, dest );
	}

	@Override
	public int hashCode()
	{
		return hash_code_;
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof MoveAction) ) {
			return false;
		}
		
		final MoveAction that = (MoveAction) obj;
		return src == that.src && dest == that.dest;
	}

	@Override
	public String toString()
	{
		return "MoveAction[" + src + "; " + dest + "]";
	}
}
