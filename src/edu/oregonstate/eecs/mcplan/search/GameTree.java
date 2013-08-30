/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public interface GameTree<S, A extends VirtualConstructor<A>>
	extends Runnable
{
	public abstract StateNode<S, A> root();
	
	/**
	 * Given an expanded state node, computes the value of the state. In a
	 * one-player game, this is usually the max-Q value. In a multiplayer game,
	 * it might be minimax or something else.
	 * @param s
	 * @return
	 */
	public abstract double[] backup( final StateNode<S, A> s );
}
