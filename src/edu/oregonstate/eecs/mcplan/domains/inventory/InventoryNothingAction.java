/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.inventory;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public class InventoryNothingAction extends InventoryAction
{
	private boolean done = false;
	
	@Override
	public void undoAction( final InventoryState s )
	{ done = false; }

	@Override
	public void doAction( final RandomGenerator rng, final InventoryState s )
	{ done = true; }

	@Override
	public boolean isDone()
	{
		return done;
	}

	@Override
	public InventoryAction create()
	{
		return new InventoryNothingAction();
	}

	@Override
	public double reward()
	{
		return 0;
	}
	
	@Override
	public int hashCode()
	{
		return 3;
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof InventoryNothingAction;
	}
	
	@Override
	public String toString()
	{
		return "Nothing";
	}
}
