/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.fuelworld;

import edu.oregonstate.eecs.mcplan.StateSpace;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class FuelWorldStateSpace extends StateSpace<FuelWorldState>
{
	public final int Nlocations;
	public final int fuel_capacity;
	
	public final FuelWorldState s0;
	
	public FuelWorldStateSpace( final FuelWorldState s0 )
	{
		Nlocations = s0.adjacency.size();
		fuel_capacity = s0.fuel_capacity;
		
		this.s0 = s0;
	}
	
	@Override
	public int cardinality()
	{
		// +1 because fuel ranges from [0, fuel_capacity] inclusive
		return Nlocations * (fuel_capacity + 1);
	}

	@Override
	public boolean isFinite()
	{ return true; }

	@Override
	public boolean isCountable()
	{ return true; }

	@Override
	public Generator<FuelWorldState> generator()
	{
		return new G();
	}
	
	// -----------------------------------------------------------------------
	
	private final class G extends Generator<FuelWorldState>
	{
		private int location = 0;
		private int fuel = 0;
		
		@Override
		public boolean hasNext()
		{
			return location < Nlocations && fuel <= fuel_capacity;
		}

		@Override
		public FuelWorldState next()
		{
			final FuelWorldState s = new FuelWorldState( s0.rng, s0.adjacency, s0.goal, s0.fuel_depots );
			s.location = location;
			s.fuel = fuel;
			
			fuel += 1;
			if( fuel > fuel_capacity ) {
				fuel = 0;
				location += 1;
			}
			
			return s;
		}
		
	}

}
