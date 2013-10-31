/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;



/**
 * @author jhostetler
 *
 */
public class DefaultMctsVisitor<S, X, A extends VirtualConstructor<A>>
	implements MctsVisitor<S, X, A>
{
	private int nagents_ = 0;
	
	protected int nagents()
	{ return nagents_; }
	
	@Override
	public void startEpisode( final S s, final int nagents, final int[] turn )
	{
		nagents_ = nagents;
	}

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
	public double[] terminal( final S s, final int[] turn )
	{
		return new double[nagents()];
	}

	@Override
	public boolean isTerminal( final S s, final int[] turn )
	{ return false; }

	@Override
	public void checkpoint()
	{ }

	@Override
	public boolean halt()
	{
		return false;
	}
}
