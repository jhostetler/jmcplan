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
public abstract class ActionNode<S, A extends VirtualConstructor<A>>
	extends GameTreeNode<S, A>
{
	private final JointAction<A> a_;
	public final int nagents;
	
	public ActionNode( final A... a )
	{
		this( new JointAction<A>( a ) );
	}
	
	public ActionNode( final JointAction<A> a )
	{
		a_ = a;
		nagents = a_.size();
	}
	
	// FIXME: This is a temporary fix
	protected ActionNode( final JointAction<A> a, final int nagents )
	{
		a_ = a;
		this.nagents = nagents;
	}
	
	public final A a( final int i )
	{ return a_.get( i ); }
	
	public final JointAction<A> a()
	{ return a_; }
	
//	public abstract StateNode<S, A> getStateNode( final Representation<S> x, final int[] turn );
	
	@Override
	public abstract Generator<? extends StateNode<S, A>> successors();
	
	public abstract int n();
	
	public abstract double[] r();
	
	public abstract double r( final int i );
	
	public abstract double[] rvar();
	
	public abstract double rvar( final int i );
	
	// FIXME: In the literature, it's always:
	// Q(s, a) = R(s, a) + E[V(s')|a]
	// But currently, q() returns only the E[V(s')|a] part! It should have
	// r() added to it, and you should come up with different method names
	// if you want to be able to return the components separately!
	public abstract double[] q();
	
	public abstract double q( final int i );
	
	public abstract double[] qvar();
	
	public abstract double qvar( final int i );
	
	@Override
	public String toString()
	{ return (a_ != null ? a_.toString() : "null"); }

	@Override
	public void accept( final GameTreeVisitor<S, A> visitor )
	{ visitor.visit( this ); }
}
