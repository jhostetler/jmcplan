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
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class AggregatingActionNode<S extends State, A extends VirtualConstructor<A>>
	extends MutableActionNode<S, A>
{
	private final Representer<S, ? extends FactoredRepresentation<S>> factored_repr_;
	private final Representer<FactoredRepresentation<S>, Representation<S>> abstract_repr_;
	
	public <X extends FactoredRepresentation<S>>
	AggregatingActionNode( final JointAction<A> a, final int nagents,
						   final Representer<S, ? extends FactoredRepresentation<S>> base_repr,
						   final Representer<FactoredRepresentation<S>, Representation<S>> abstract_repr )
	{
		super( a, nagents, base_repr );
		factored_repr_ = base_repr;
		abstract_repr_ = abstract_repr;
	}
	
	@Override
	public AggregatingActionNode<S, A> create()
	{
		return new AggregatingActionNode<S, A>( a(), nagents, factored_repr_.create(), abstract_repr_.create() );
	}
	
	@Override
	public MutableStateNode<S, A> successor(
			final S s, final int nagents, final int[] turn, final ActionGenerator<S, JointAction<A>> action_gen )
	{
//		System.out.println( "successor( " + s + " )" );
		if( s.isTerminal() ) {
//			System.out.println( "\tTerminal" );
			
//			final IdentityRepresentation<S> x = new IdentityRepresentation<S>( s.toString() );
//			final MutableStateNode<S, A> leaf = new LeafStateNode<S, A>( x, nagents, turn );
//			attachSuccessor( x, turn, leaf );
			
			final LeafRepresentation<S> x = new LeafRepresentation<S>();
			MutableStateNode<S, A> leaf = getStateNode( x, turn );
			if( leaf == null ) {
				leaf = new LeafStateNode<S, A>( nagents, turn );
				attachSuccessor( x, turn, leaf );
			}
			
			return leaf;
		}
		
		final FactoredRepresentation<S> x = factored_repr_.encode( s );
		final Representation<S> ab = abstract_repr_.encode( x );
		
		final MutableStateNode<S, A> sn = getStateNode( ab, turn );
		if( sn != null ) {
//			System.out.println( "Hit " + x );
			return sn;
		}
		else {
//			System.out.println( "Miss " + x );
			final MutableStateNode<S, A> succ = createSuccessor( s, ab, nagents, turn, action_gen );
			attachSuccessor( ab, turn, succ );
			return succ;
		}
	}

	protected MutableStateNode<S, A> createSuccessor(
			final S s, final Representation<S> x, final int nagents, final int[] turn,
			final ActionGenerator<S, JointAction<A>> action_gen )
	{
		return new SimpleMutableStateNode<S, A>( x, nagents, turn, action_gen ) {
			@Override
			protected MutableActionNode<S, A> createSuccessor(
				final JointAction<A> a, final int nagents, final Representer<S, ? extends Representation<S>> repr )
			{
				return new AggregatingActionNode<S, A>( a, nagents, factored_repr_.create(), abstract_repr_.create() );
			}
		};
	}
}
