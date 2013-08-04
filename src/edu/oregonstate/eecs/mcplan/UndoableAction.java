/**
 * 
 */
package edu.oregonstate.eecs.mcplan;


/**
 * @author jhostetler
 *
 */
public interface UndoableAction<S> extends Action<S>, VirtualConstructor<UndoableAction<S>>
{
	public abstract void undoAction( final S s );
	@Override
	public abstract UndoableAction<S> create();
}
