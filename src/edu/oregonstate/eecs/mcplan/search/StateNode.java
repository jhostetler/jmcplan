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
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public abstract class StateNode<S, A extends VirtualConstructor<A>>
	extends GameTreeNode<S, A>
{
//	public final Representation<S> x;
	public final int nagents;
	public final int[] turn;
	
	protected final double[] vhat_;
	
	public StateNode( /*final Representation<S> x,*/ final int nagents, final int[] turn )
	{
		//assert( nagents == 2 ); // TODO:
//		this.x = x;
		this.nagents = nagents;
		this.turn = turn;
		vhat_ = new double[nagents];
	}
	
	public abstract int n();
	
//	public abstract double[] v();
	
	public abstract double[] r();

	public abstract double r( final int i );
	
	public abstract double[] rvar();
	
	public abstract double rvar( final int i );
	
	@Override
	public abstract Generator<? extends ActionNode<S, A>> successors();
	
	public abstract ActionNode<S, A> getActionNode( final JointAction<A> a );
	
//	public ActionNode<S, A> bestAction( final RandomGenerator rng )
//	{
//		// Find all actions with maximal Q-value, and choose one at
//		// random. Randomness is necessary because in the late game, if
//		// the rollout policy always wins, all actions look the same, but
//		// the UCT policy might always pick one that never actually wins
//		// the game due to fixed action generator ordering.
//		double max_q = -Double.MAX_VALUE;
//		final ArrayList<ActionNode<S, A>> action_pool = new ArrayList<ActionNode<S, A>>();
//		for( final ActionNode<S, A> an : Fn.in( successors() ) ) {
////			log.info( "Action {}: n = {}, q = {}, 95% = {}",
////					  e.getKey(), e.getValue().n(), e.getValue().q(), 2 * Math.sqrt( e.getValue().qvar() ) );
//			final double q = an.q( turn );
//			if( q > max_q ) {
//				max_q = q;
//				action_pool.clear();
//			}
//			if( q == max_q ) {
//				action_pool.add( an );
//			}
//		}
////		log.info( "Action pool: {}", action_pool );
//		final int random_action = rng.nextInt( action_pool.size() );
//		System.out.println( "--> Selected " + action_pool.get( random_action ) );
//		return action_pool.get( random_action );
//	}

	@Override
	public void accept( final GameTreeVisitor<S, A> visitor )
	{
		visitor.visit( this );
	}
}