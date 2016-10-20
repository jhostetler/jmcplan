/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.taxi;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;

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
	public void doAction( final RandomGenerator rng_unused, final TaxiState s )
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
