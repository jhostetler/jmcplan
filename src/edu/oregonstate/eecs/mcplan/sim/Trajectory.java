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
package edu.oregonstate.eecs.mcplan.sim;

import edu.oregonstate.eecs.mcplan.State;


/**
 * Utility functions for working with trajectories. A trajectory is a
 * chain-structured state-action graph.
 * 
 * @author jhostetler
 */
public final class Trajectory
{
	private Trajectory()
	{ }
	
	public static <S extends State, A> void closeStates( final StateNode<S, A> sn )
	{
		final StateActionGraphVisitor<S, A> v = new StateActionGraphVisitor<S, A>() {
			@Override
			public void visitStateNode( final StateNode<S, A> sn )
			{
				sn.s.close();
			}
		};
		new TrajectoryTraversal<>( sn, v ).run();
	}
	
	public static <S, A> StateNode<S, A> nextState( final StateNode<S, A> sn )
	{
		for( final ActionNode<S, A> an : sn.successors() ) {
			for( final StateNode<S, A> snprime : an.successors() ) {
				return snprime;
			}
		}
		return null;
	}
	
	public static <S, A> double sumReward( final StateNode<S, A> sn )
	{
		final SumRewardsVisitor<S, A> v = new SumRewardsVisitor<>();
		final TrajectoryTraversal<S, A> traversal = new TrajectoryTraversal<>( sn, v );
		traversal.run();
		return v.getSum();
	}
}
