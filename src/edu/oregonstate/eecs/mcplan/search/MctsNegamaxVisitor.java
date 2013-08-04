/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;


/**
 * @author jhostetler
 *
 */
public interface MctsNegamaxVisitor<S, A>
{
	public abstract void startEpisode( final S s );
	public abstract boolean startRollout( final S s );
	public abstract void startTree( final S s );
	public abstract void treeAction( final A a, final S sprime );
	public abstract void treeDepthLimit( final S s );
	public abstract void startDefault( final S s );
	public abstract void defaultAction( final A a, final S sprime );
	public abstract void defaultDepthLimit( final S s );
	public abstract void depthLimit( final S s );
	public abstract double terminal( final S s );
	public abstract boolean isTerminal( final S s );
}
