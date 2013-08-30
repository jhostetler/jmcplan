/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import edu.oregonstate.eecs.mcplan.Option;
import edu.oregonstate.eecs.mcplan.UndoableAction;

/**
 * @author jhostetler
 *
 */
public interface OptionListener<S, A extends UndoableAction<S>> extends SimultaneousMoveListener<S, A>
{
	public abstract void optionTerminated( final S s, final int i, final Option<S, A> o );
	public abstract void optionInitiated( final S s, final int i, final Option<S, A> o );
}
