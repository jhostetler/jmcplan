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
public interface MctsVisitor<S, X, A extends VirtualConstructor<A>>
{
	public abstract void startEpisode( final S s, final int nagents, final int[] turn );
	public abstract boolean startRollout( final S s, final int[] turn );
	public abstract void startTree( final S s, final int[] turn );
	public abstract void treeAction( final JointAction<A> a, final S sprime, final int[] next_turn );
	public abstract void treeDepthLimit( final S s, final int[] turn );
	public abstract void startDefault( final S s, final int[] turn );
	public abstract void defaultAction( final JointAction<A> a, final S sprime, final int[] next_turn );
	public abstract void defaultDepthLimit( final S s, final int[] turn );
	public abstract void depthLimit( final S s, final int[] turn );
	public abstract double[] terminal( final S s, final int[] turn );
	public abstract boolean isTerminal( final S s, final int[] turn );
	
	public abstract void checkpoint();
	public abstract boolean halt();
}
