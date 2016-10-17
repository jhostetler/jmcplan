/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.fuelworld;

import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public abstract class FuelWorldAction extends UndoableAction<FuelWorldState> implements VirtualConstructor<FuelWorldAction>
{
	@Override
	public abstract int hashCode();
	
	@Override
	public abstract boolean equals( final Object obj );
	
	@Override
	public abstract String toString();
}
