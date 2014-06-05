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
public class DefaultEpisodeListener<S, A extends VirtualConstructor<A>> implements EpisodeListener<S, A>
{
	@Override
	public <P extends Policy<S, JointAction<A>>>
	void startState( final S s, final double[] r, final P pi )
	{ }

	@Override
	public void preGetAction()
	{ }

	@Override
	public void postGetAction( final JointAction<A> a )
	{ }

	@Override
	public void onActionsTaken( final S sprime, final double[] r )
	{ }

	@Override
	public void endState( final S s )
	{ }
}
