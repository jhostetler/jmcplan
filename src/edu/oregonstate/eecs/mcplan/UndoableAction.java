/**
 * 
 */
package edu.oregonstate.eecs.mcplan;


/**
 * @author jhostetler
 *
 */
public abstract class UndoableAction<S> implements Action<S>
{
	public abstract boolean isDone();
	public abstract void undoAction( final S s );
	
	public final void doAction( final S s )
	{
		doAction( null, s );
	}
}
