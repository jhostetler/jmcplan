/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.Policy;

/**
 * @author jhostetler
 *
 */
public interface SimultaneousMoveListener<S, A>
{
	public abstract <P extends Policy<S, A>> void startState( final S s, final ArrayList<P> policies );
	public abstract void preGetAction( final int player );
	public abstract void postGetAction( final int player, final A action );
	public abstract void onActionsTaken( final S sprime );
	public abstract void endState( final S s );
}
