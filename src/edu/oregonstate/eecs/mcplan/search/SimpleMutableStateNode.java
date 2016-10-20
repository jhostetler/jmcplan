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

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class SimpleMutableStateNode<S extends State, A extends VirtualConstructor<A>>
	extends MutableStateNode<S, A>
{
	public final Representation<S> x;
	
	public SimpleMutableStateNode( final Representation<S> x, final int nagents, final int[] turn,
							 final ActionGenerator<S, JointAction<A>> action_gen )
	{
		super( /*x,*/ nagents, turn, action_gen );
		this.x = x;
	}
	
	
	
	@Override
	public MutableActionNode<S, A> successor( final JointAction<A> a, final int nagents,
											  final Representer<S, ? extends Representation<S>> repr )
	{
		final MutableActionNode<S, A> an = getActionNode( a );
		if( an != null ) {
			return an;
		}
		else {
			final MutableActionNode<S, A> succ = createSuccessor( a, nagents, repr.create() );
			attachSuccessor( a, succ );
			return succ;
		}
	}
	
	protected MutableActionNode<S, A> createSuccessor(
			final JointAction<A> a, final int nagents, final Representer<S, ? extends Representation<S>> repr )
	{
		return new SimpleMutableActionNode<S, A>( a, nagents, repr );
	}
	
	@Override
	public String toString()
	{
		return "StateNode[" + x + "]";
	}

}