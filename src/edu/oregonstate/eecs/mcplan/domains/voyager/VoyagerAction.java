/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public abstract class VoyagerAction extends UndoableAction<VoyagerState> implements VirtualConstructor<VoyagerAction>
{

}
