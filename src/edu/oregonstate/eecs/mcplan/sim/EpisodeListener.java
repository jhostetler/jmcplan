/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import edu.oregonstate.eecs.mcplan.Policy;

/**
 * @author jhostetler
 *
 */
public interface EpisodeListener<S, A>
{
	public abstract <P extends Policy<S, A>> void startState( final S s, final P policies );
	public abstract void preGetAction();
	public abstract void postGetAction( final A action );
	public abstract void onActionsTaken( final S sprime );
	public abstract void endState( final S s );
}
