/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.inventory;

import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public abstract class InventoryAction implements UndoableAction<InventoryState>, VirtualConstructor<InventoryAction>
{
	public abstract double reward();
}
