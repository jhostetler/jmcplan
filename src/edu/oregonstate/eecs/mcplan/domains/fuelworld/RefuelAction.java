/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.fuelworld;

/**
 * @author jhostetler
 *
 */
public class RefuelAction extends FuelWorldAction
{
	private int old_fuel_ = -1;

	private boolean done_ = false;
	
	@Override
	public void undoAction( final FuelWorldState s )
	{
		assert( done_ );
		s.fuel = old_fuel_;
		done_ = false;
	}

	@Override
	public void doAction( final FuelWorldState s )
	{
		assert( !done_ );
		assert( s.fuel_depots.contains( s.location ) );
		
		old_fuel_ = s.fuel;
		s.fuel = s.fuel_capacity;
		
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
		return new RefuelAction();
	}

	@Override
	public int hashCode()
	{
		return 5;
	}

	@Override
	public boolean equals( final Object obj )
	{
		return (obj instanceof RefuelAction);
	}

	@Override
	public String toString()
	{
		return "RefuelAction";
	}
}
