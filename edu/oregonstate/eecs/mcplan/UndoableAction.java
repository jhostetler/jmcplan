/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import edu.oregonstate.eecs.mcplan.agents.galcon.Action;

/**
 * @author jhostetler
 *
 */
public interface UndoableAction<S, A> extends Action<S, A>
{
	public abstract void undoAction( final S s );
}
