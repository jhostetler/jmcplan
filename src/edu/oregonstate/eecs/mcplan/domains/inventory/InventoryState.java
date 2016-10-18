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
