/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.taxi;

import java.util.Arrays;

/**
 * @author jhostetler
 *
 */
public class PickupAction extends TaxiAction
{
	private boolean done_ = false;
	
	private int old_passenger_ = -1;
	private boolean old_illegal_ = false;
	
	@Override
	public void undoAction( final TaxiState s )
	{
		assert( done_ );
		s.passenger = old_passenger_;
		s.illegal_pickup_dropoff = old_illegal_;
		done_ = false;
	}

	@Override
	public void doAction( final TaxiState s )
	{
		assert( !done_ );
		old_passenger_ = s.passenger;
		old_illegal_ = s.illegal_pickup_dropoff;
		if( s.passenger != TaxiState.IN_TAXI && Arrays.equals( s.taxi, s.locations.get( s.passenger ) ) ) {
			s.passenger = TaxiState.IN_TAXI;
		}
		else {
			s.illegal_pickup_dropoff = true;
		}
		done_ = true;
	}

	@Override
	public boolean isDone()
	{
		return done_;
	}

	@Override
	public TaxiAction create()
	{
		return new PickupAction();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		return (obj instanceof PickupAction);
	}
	
	@Override
	public int hashCode()
	{
		return 29;
	}
	
	@Override
	public String toString()
	{
		return "PickupAction";
	}
}
