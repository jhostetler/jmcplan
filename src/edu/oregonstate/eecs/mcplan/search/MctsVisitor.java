/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;



/**
 * Allows you to monitor the MCTS tree construction process. One important
 * use case is when you need access to the primitive state of type S, rather
 * than the representation of S that the tree is being built over.
 * 
 * TODO: Re-do this interface, as it has become stale and bloated!
 * 
 * @see GameTreeVisitor
 * 
 * @author jhostetler
 */
public interface MctsVisitor<S, A extends VirtualConstructor<A>>
{
	/**
	 * Called on episode start.
	 * @param s
	 * @param nagents
	 * @param turn
	 */
	public abstract void startEpisode( final S s, final int nagents, final int[] turn );
	
	/**
	 * Called when starting rollout. Will be renamed to have something to do
	 * with "evaluation" in the future.
	 * @deprecated
	 * @param s
	 * @param turn
	 * @return
	 */
	@Deprecated
	public abstract boolean startRollout( final S s, final int[] turn );
	
	/**
	 * TODO: What is this for?
	 * @deprecated
	 * @param s
	 * @param turn
	 */
	@Deprecated
	public abstract void startTree( final S s, final int[] turn );
	
	/**
	 * Called after executing an action within the search tree (ie. an action
	 * chosen according to the UCT rule rather than during a rollout).
	 * @param a
	 * @param sprime
	 * @param next_turn
	 */
	public abstract void treeAction( final JointAction<A> a, final S sprime, final int[] next_turn );
	
	/**
	 * Called when the search reaches the depth limit, if any.
	 * TODO: You should probably pass the depth to this function!
	 * @param s
	 * @param turn
	 */
	public abstract void treeDepthLimit( final S s, final int[] turn );
	
	/**
	 * Called at start of rollout.
	 * @deprecated Because rollout is no longer part of MCTS proper.
	 * @param s
	 * @param turn
	 */
	@Deprecated
	public abstract void startDefault( final S s, final int[] turn );
	
	/**
	 * Called when a rollout action is taken.
	 * @deprecated Because rollout is no longer part of MCTS proper.
	 * @param s
	 * @param turn
	 */
	@Deprecated
	public abstract void defaultAction( final JointAction<A> a, final S sprime, final int[] next_turn );
	
	/**
	 * Called when the rollout depth limit is reached.
	 * @deprecated Because rollout is no longer part of MCTS proper.
	 * @param s
	 * @param turn
	 */
	@Deprecated
	public abstract void defaultDepthLimit( final S s, final int[] turn );
	
	/**
	 * TODO: Presumably this was called when *either* depth limit was reached?
	 * @deprecated Because rollout is no longer part of MCTS proper.
	 * @param s
	 * @param turn
	 */
	@Deprecated
	public abstract void depthLimit( final S s, final int[] turn );
	
	/**
	 * Called periodically. Useful for implementing wall-clock time limits.
	 */
	public abstract void checkpoint();
	
	/**
	 * Return 'true' to halt the search. Useful for implementing wall-clock
	 * time limits.
	 * @return
	 */
	public abstract boolean halt();
}
