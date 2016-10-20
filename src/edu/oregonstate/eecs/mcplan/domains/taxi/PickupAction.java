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
public class PickupAction extends TaxiAction
{
	private boolean done_ = false;
	
	private int old_passenger_ = -1;
	private boolean old_illegal_ = false;
	private boolean old_pickup_success_ = false;
	
	@Override
	public void undoAction( final TaxiState s )
	{
		assert( done_ );
		s.passenger = old_passenger_;
		s.pickup_success = old_pickup_success_;
		s.illegal_pickup_dropoff = old_illegal_;
		done_ = false;
	}

	@Override
	public void doAction( final RandomGenerator rng_unused, final TaxiState s )
	{
		assert( !done_ );
		old_passenger_ = s.passenger;
		old_pickup_success_ = s.pickup_success;
		old_illegal_ = s.illegal_pickup_dropoff;
		if( s.passenger != TaxiState.IN_TAXI && Arrays.equals( s.taxi, s.locations.get( s.passenger ) ) ) {
			s.passenger = TaxiState.IN_TAXI;
			s.pickup_success = true;
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
