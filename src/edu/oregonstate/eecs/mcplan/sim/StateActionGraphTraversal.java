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

import java.util.ArrayList;
import java.util.List;

/**
 * Traverses a state-action graph defined by StateNode and ActionNode objects,
 * and fires events when encountering nodes.
 * 
 * @author jhostetler
 */
public abstract class StateActionGraphTraversal<S, A> implements Runnable
{
	protected final StateNode<S, A> s0;
	private final List<StateActionGraphVisitor<S, A>> visitors = new ArrayList<>();
	
	public StateActionGraphTraversal( final StateNode<S, A> s0 )
	{
		this.s0 = s0;
	}
	
	public StateActionGraphTraversal( final StateNode<S, A> s0, final StateActionGraphVisitor<S, A> visitor )
	{
		this( s0 );
		visitors.add( visitor );
	}
	
	public void addVisitor( final StateActionGraphVisitor<S, A> visitor )
	{
		visitors.add( visitor );
	}
	
	protected void fireStateNode( final StateNode<S, A> sn )
	{
		for( final StateActionGraphVisitor<S, A> visitor : visitors ) {
			visitor.visitStateNode( sn );
		}
	}
	
	protected void fireActionNode( final ActionNode<S, A> an )
	{
		for( final StateActionGraphVisitor<S, A> visitor : visitors ) {
			visitor.visitActionNode( an );
		}
	}
}
