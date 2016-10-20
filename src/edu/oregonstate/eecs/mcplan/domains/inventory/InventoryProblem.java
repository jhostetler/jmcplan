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

import org.apache.commons.math3.distribution.GeometricDistribution;



/**
 * @author jhostetler
 *
 */
public abstract class InventoryProblem
{
	public final int Nproducts;
	public final int[] price;
	public final int max_inventory;
	public final double warehouse_cost;
	public final int min_order;
	public final int max_order;
	public final int[] cost;
	public final double delivery_probability;
	public final int max_demand;
	
	public InventoryProblem( final int Nproducts, final int[] price,
						     final int max_inventory, final double warehouse_cost,
						     final int min_order, final int max_order,
						     final int[] cost,
						     final double delivery_probability, final int max_demand )
	{
		this.Nproducts = Nproducts;
		this.price = price;
		this.max_inventory = max_inventory;
		this.warehouse_cost = warehouse_cost;
		this.min_order = min_order;
		this.max_order = max_order;
		this.cost = cost;
		this.delivery_probability = delivery_probability;
		this.max_demand = max_demand;
	}
	
	public abstract int[] sampleNextDemand( final InventoryState s );
	
	/**
	 * Demand is IID uniform and delivery probability is low.
	 * @return
	 */
	public static InventoryProblem TwoProducts()
	{
		final int Nproducts = 2;
		final int max_inventory = 10;
		final double warehouse_cost = 1;
		final int min_order = 2;
		final int max_order = 5;
		final int cost[] = { 0, 0 };
		final double delivery_probability = 0.3;
		final int max_demand = 7;
		final int[] price = new int[] { 3, 5 };
		
		final InventoryProblem problem = new InventoryProblem(
			Nproducts, price, max_inventory, warehouse_cost, min_order, max_order, cost, delivery_probability, max_demand )
		{
			@Override
			public int[] sampleNextDemand( final InventoryState s )
			{
				final int[] dprime = new int[s.problem.Nproducts];
				for( int i = 0; i < s.problem.Nproducts; ++i ) {
					dprime[i] = s.rng.nextInt( s.problem.max_demand + 1 );
				}
				return dprime;
			}
		};
		return problem;
	}
	
	/**
	 * Demand follows a bounded random walk with uniform increments of bounded
	 * size. Delivery probability is fairly high, and warehouse costs are
	 * small in proportion to prices.
	 * @return
	 */
	public static InventoryProblem Dependent()
	{
		final int Nproducts = 2;
		final int max_inventory = 30;
		final double warehouse_cost = 1;
		final int min_order = 2;
		final int max_order = 5;
		final int cost[] = { 0, 0 };
		final double delivery_probability = 0.7;
		final int max_demand = 20;
		final int[] price = new int[] { 6, 10 };
		
		final int range = 5;
		assert( range % 2 == 1 );
		final InventoryProblem problem = new InventoryProblem(
			Nproducts, price, max_inventory, warehouse_cost, min_order, max_order, cost, delivery_probability, max_demand )
		{
			@Override
			public int[] sampleNextDemand( final InventoryState s )
			{
				final int[] dprime = new int[s.problem.Nproducts];
				for( int i = 0; i < s.problem.Nproducts; ++i ) {
					final int increment = s.rng.nextInt( range );
					final int r = s.demand[i] - (range / 2) + increment;
					if( r < 0 ) {
						dprime[i] = 0;
					}
					else if( r > max_demand ) {
						dprime[i] = max_demand;
					}
					else {
						dprime[i] = r;
					}
				}
				return dprime;
			}
		};
		return problem;
	}
	
	/**
	 * Demand follows a bounded random walk with uniform increments of bounded
	 * size. Delivery probability is fairly high, and warehouse costs are
	 * small in proportion to prices.
	 * @return
	 */
	public static InventoryProblem Geometric()
	{
		final int Nproducts = 2;
		final int max_inventory = 30;
		final double warehouse_cost = 1;
		final int min_order = 2;
		final int max_order = 5;
		final int cost[] = { 1, 2 };
		final double delivery_probability = 0.7;
		final int max_demand = 20;
		final int[] price = new int[] { 9, 15 };
		
		final InventoryProblem problem = new InventoryProblem(
			Nproducts, price, max_inventory, warehouse_cost, min_order, max_order, cost, delivery_probability, max_demand )
		{
			@Override
			public int[] sampleNextDemand( final InventoryState s )
			{
				final GeometricDistribution f = new GeometricDistribution( s.rng, 0.4 );
				final int[] dprime = new int[s.problem.Nproducts];
				for( int i = 0; i < s.problem.Nproducts; ++i ) {
					dprime[i] = Math.min( f.sample(), max_demand );
				}
				return dprime;
			}
		};
		return problem;
	}
	
	/**
	 * Almost the same as 'Geometric', but with a small warehouse capacity and
	 * higher delivery probability.
	 * We hope that this will make lookahead more useful.
	 * @return
	 */
	public static InventoryProblem Geometric2()
	{
		final int Nproducts = 2;
		final int max_inventory = 4;
		final double warehouse_cost = 1;
		final int min_order = 2;
		final int max_order = 5;
		final int cost[] = { 1, 2 };
		final double delivery_probability = 0.8;
		final int max_demand = 20;
		final double demand_p = 0.4;
		final int[] price = new int[] { 9, 15 };
		
		final InventoryProblem problem = new InventoryProblem(
			Nproducts, price, max_inventory, warehouse_cost, min_order, max_order, cost, delivery_probability, max_demand )
		{
			@Override
			public int[] sampleNextDemand( final InventoryState s )
			{
				final GeometricDistribution f = new GeometricDistribution( s.rng, demand_p );
				final int[] dprime = new int[s.problem.Nproducts];
				for( int i = 0; i < s.problem.Nproducts; ++i ) {
					dprime[i] = Math.min( f.sample(), max_demand );
				}
				return dprime;
			}
		};
		return problem;
	}
}
