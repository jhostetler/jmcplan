/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;



/**
 * A stub implementation of MctsVisitor. Use this when you need a visitor that
 * does nothing, or inherit from it if you only need to override one or
 * two methods.
 * 
 * @author jhostetler
 */
public class DefaultMctsVisitor<S, A extends VirtualConstructor<A>>
	implements MctsVisitor<S, A>
{
	@Override
	public void startEpisode( final S s, final int nagents, final int[] turn )
	{ }

	@Override
	public boolean startRollout( final S s, final int[] turn )
	{ return true; }

	@Override
	public void startTree( final S s, final int[] turn )
	{ }

	@Override
	public void treeAction( final JointAction<A> a, final S sprime, final int[] next_turn )
	{ }

	@Override
	public void treeDepthLimit( final S s, final int[] turn )
	{ }

	@Override
	public void startDefault( final S s, final int[] turn )
	{ }

	@Override
	public void defaultAction( final JointAction<A> a, final S sprime, final int[] next_turn )
	{ }

	@Override
	public void defaultDepthLimit( final S s, final int[] turn )
	{ }

	@Override
	public void depthLimit( final S s, final int[] turn )
	{ }

	@Override
	public void checkpoint()
	{ }

	@Override
	public boolean halt()
	{
		return false;
	}
}
