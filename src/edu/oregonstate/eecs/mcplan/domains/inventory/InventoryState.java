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
package edu.oregonstate.eecs.mcplan.domains.inventory;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class InventoryState implements State
{
	public final RandomGenerator rng;
	
	public final InventoryProblem problem;
	
	public final int[] demand;
	public final int[] inventory;
	public final int[] orders;
	
	public double r = 0;
	
	public InventoryState( final RandomGenerator rng, final InventoryProblem problem )
	{
		this.rng = rng;
		this.problem = problem;
		demand = new int[problem.Nproducts];
		inventory = new int[problem.Nproducts];
		orders = new int[problem.Nproducts];
	}
	
	public InventoryState copy()
	{
		final InventoryState copy = new InventoryState( rng, problem );
		Fn.memcpy( copy.demand, demand );
		Fn.memcpy( copy.inventory, inventory );
		Fn.memcpy( copy.orders, orders );
		return copy;
	}
	
	@Override
	public boolean isTerminal()
	{
		return false;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "i: " ).append( Arrays.toString( inventory ) )
		  .append( ", o: " ).append( Arrays.toString( orders ) )
		  .append( ", d: " ).append( Arrays.toString( demand ) );
		return sb.toString();
	}

	@Override
	public void close()
	{ }
}
