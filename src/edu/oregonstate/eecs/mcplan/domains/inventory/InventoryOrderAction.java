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

import org.apache.commons.math3.random.RandomGenerator;



/**
 * @author jhostetler
 *
 */
public class InventoryOrderAction extends InventoryAction
{
	public final int product;
	public final int quantity;
	public final int cost;
	
	private boolean done = false;
	
	public InventoryOrderAction( final int product, final int quantity, final int cost )
	{
		this.product = product;
		this.quantity = quantity;
		this.cost = cost;
	}
	
	@Override
	public InventoryOrderAction create()
	{
		return new InventoryOrderAction( product, quantity, cost );
	}
	
	@Override
	public double reward()
	{
		// Per-order cost
		return -(1 + quantity*cost);
	}

	@Override
	public void doAction( final RandomGenerator rng, final InventoryState s )
	{
		assert( !done );
		s.orders[product] += quantity;
		done = true;
	}

	@Override
	public boolean isDone()
	{
		return done;
	}

	@Override
	public void undoAction( final InventoryState s )
	{
		assert( done );
		s.orders[product] -= quantity;
		done = false;
	}
	
	@Override
	public int hashCode()
	{
		return 5 * (7 + product * (11 + quantity));
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof InventoryOrderAction) ) {
			return false;
		}
		final InventoryOrderAction that = (InventoryOrderAction) obj;
		return product == that.product && quantity == that.quantity;
	}
	
	@Override
	public String toString()
	{
		return "OrderAction[" + product + ", " + quantity + "]";
	}
}
