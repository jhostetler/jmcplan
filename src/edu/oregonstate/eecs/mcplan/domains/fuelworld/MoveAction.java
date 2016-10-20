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
