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

import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * Traverses the "left-most" trajectory in the graph, that is the trajectory
 * formed by following the first successor of every node. Will run forever if
 * the leftmost trajectory is a cycle.
 */
public class TrajectoryTraversal<S, A> extends StateActionGraphTraversal<S, A>
{
	public TrajectoryTraversal( final StateNode<S, A> s0 )
	{
		super( s0 );
	}
	
	public TrajectoryTraversal( final StateNode<S, A> s0, final StateActionGraphVisitor<S, A> visitor )
	{
		super( s0, visitor );
	}

	@Override
	public void run()
	{
		StateNode<S, A> s = s0;
		fireStateNode( s );
		while( !s.isTerminal() ) {
			final ActionNode<S, A> a = Fn.head( s.successors() );
			fireActionNode( a );
			s = Fn.head( a.successors() );
			fireStateNode( s );
		}
	}
}
