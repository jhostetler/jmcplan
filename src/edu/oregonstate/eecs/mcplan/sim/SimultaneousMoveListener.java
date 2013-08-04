/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.UndoableAction;

/**
 * @author jhostetler
 *
 */
public interface SimultaneousMoveListener<S, A extends UndoableAction<S>>
{
	public abstract <P extends Policy<S, A>> void startState( final S s, final ArrayList<P> policies );
	public abstract void preGetAction( final int player );
	public abstract void postGetAction( final int player, final UndoableAction<S> action );
	public abstract void onActionsTaken( final S sprime );
	public abstract void endState( final S s );
}
