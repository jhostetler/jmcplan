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
