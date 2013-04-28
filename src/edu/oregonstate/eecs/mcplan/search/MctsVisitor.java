/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

/**
 * @author jhostetler
 *
 */
public interface MctsVisitor<S, A>
{
	public abstract void startEpisode( final S s );
	public abstract void treeAction( final A a, final S sprime );
	public abstract void treeDepthLimit( final S s );
	public abstract void rolloutAction( final A a, final S sprime );
	public abstract void rolloutDepthLimit( final S s );
	public abstract void depthLimit( final S s );
	public abstract double terminal( final S s );
	public abstract boolean isTerminal( final S s );
}
