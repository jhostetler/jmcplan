/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

/**
 * @author jhostetler
 *
 */
public class DefaultMctsVisitor<S, A> implements MctsVisitor<S, A>
{
	@Override
	public void startEpisode( final S s )
	{ }

	@Override
	public void treeAction( final A a, final S sprime )
	{ }

	@Override
	public void treeDepthLimit( final S s )
	{ }

	@Override
	public void rolloutAction( final A a, final S sprime )
	{ }

	@Override
	public void rolloutDepthLimit( final S s )
	{ }

	@Override
	public void depthLimit( final S s )
	{ }

	@Override
	public double terminal( final S s )
	{
		return 0;
	}

	@Override
	public boolean isTerminal( final S s )
	{
		return false;
	}
}
