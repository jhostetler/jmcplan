/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.taxi;

import java.util.Arrays;

/**
 * @author jhostetler
 *
 */
public class PutdownAction extends TaxiAction
{
	private boolean done_ = false;
	
	private int old_passenger_ = -1;
	private boolean old_goal_ = false;
	private boolean old_pickup_success_ = false;
	private boolean old_illegal_ = false;
	
	@Override
	public void undoAction( final TaxiState s )
	{
		assert( done_ );
		
		s.passenger = old_passenger_;
		s.goal = old_goal_;
		s.pickup_success = old_pickup_success_;
		s.illegal_pickup_dropoff = old_illegal_;
		done_ = false;
	}

	@Override
	public void doAction( final TaxiState s )
	{
		assert( !done_ );
		
		old_passenger_ = s.passenger;
		old_goal_ = s.goal;
		old_pickup_success_ = s.pickup_success;
		old_illegal_ = s.illegal_pickup_dropoff;
		
		s.pickup_success = false;
		
//		if( s.passenger == TaxiState.IN_TAXI ) {
//			for( int loc_i = 0; loc_i < s.locations.size(); ++loc_i ) {
//				final int[] loc = s.locations.get( loc_i );
//				if( Arrays.equals( loc, s.taxi ) ) {
//					s.passenger = loc_i;
//					if( loc_i == s.destination ) {
//						s.goal = true;
//					}
//					done_ = true;
//					return;
//				}
//			}
//		}
		
		if( s.passenger == TaxiState.IN_TAXI ) {
			for( int loc_i = 0; loc_i < s.locations.size(); ++loc_i ) {
				final int[] loc = s.locations.get( loc_i );
				if( Arrays.equals( loc, s.taxi ) && loc_i == s.destination ) {
					s.passenger = loc_i;
					s.goal = true;
					done_ = true;
					return;
				}
			}
		}
		
		s.illegal_pickup_dropoff = true;
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
		return new PutdownAction();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		return (obj instanceof PutdownAction);
	}
	
	@Override
	public int hashCode()
	{
		return 31;
	}
	
	@Override
	public String toString()
	{
		return "PutdownAction";
	}
}
