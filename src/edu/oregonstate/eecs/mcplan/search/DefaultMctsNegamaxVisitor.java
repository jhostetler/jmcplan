/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;


/**
 * @author jhostetler
 *
 */
public class DefaultMctsNegamaxVisitor<S, A> implements MctsNegamaxVisitor<S, A>
{
	@Override
	public void startEpisode( final S s )
	{ }
	
	@Override
	public boolean startRollout( final S s )
	{ return true; }
	
	@Override
	public void startTree( final S s )
	{ }

	@Override
	public void treeAction( final A a, final S sprime )
	{ }

	@Override
	public void treeDepthLimit( final S s )
	{ }
	
	@Override
	public void startDefault( final S s )
	{ }

	@Override
	public void defaultAction( final A a, final S sprime )
	{ }

	@Override
	public void defaultDepthLimit( final S s )
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
