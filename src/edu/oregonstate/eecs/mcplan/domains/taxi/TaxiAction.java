/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.taxi;

import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public abstract class TaxiAction extends UndoableAction<TaxiState> implements VirtualConstructor<TaxiAction>
{

}
