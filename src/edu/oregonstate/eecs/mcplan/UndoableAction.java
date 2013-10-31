/**
 * 
 */
package edu.oregonstate.eecs.mcplan;


/**
 * @author jhostetler
 *
 */
public interface UndoableAction<S> extends Action<S>
{
	public abstract void undoAction( final S s );
}
