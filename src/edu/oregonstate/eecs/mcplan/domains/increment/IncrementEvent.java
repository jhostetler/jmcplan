/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.increment;

import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public abstract class IncrementEvent extends UndoableAction<IncrementState> implements VirtualConstructor<IncrementEvent>
{

}
