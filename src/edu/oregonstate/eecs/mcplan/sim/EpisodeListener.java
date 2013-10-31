/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public interface EpisodeListener<S, A extends VirtualConstructor<A>>
{
	public abstract <P extends Policy<S, JointAction<A>>> void startState( final S s, final P pi );
	public abstract void preGetAction();
	public abstract void postGetAction( final JointAction<A> a );
	public abstract void onActionsTaken( final S sprime );
	public abstract void endState( final S s );
}
