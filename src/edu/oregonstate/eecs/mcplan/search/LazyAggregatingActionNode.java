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

import java.util.ArrayList;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * This is a "lazy" version of AggregatingActionNode. It will not begin using
 * the abstract representation until a minimum number of visits have been
 * made to the action node. At that point, it will retroactively apply the
 * abstraction to all existing children, and then use the abstraction for
 * all subsequent visits.
 * 
 * @author jhostetler
 */
public class LazyAggregatingActionNode<S extends State, A extends VirtualConstructor<A>>
	extends MutableActionNode<S, A>
{
	private final Representer<S, ? extends FactoredRepresentation<S>> factored_repr_;
	private final Representer<FactoredRepresentation<S>, Representation<S>> abstract_repr_;
	private boolean lazy_ = true;
	private final int lazy_threshold_;
	
	public LazyAggregatingActionNode( final JointAction<A> a, final int nagents,
						   			  final Representer<S, ? extends FactoredRepresentation<S>> base_repr,
						   			  final Representer<FactoredRepresentation<S>, Representation<S>> abstract_repr,
						   			  final int lazy_threshold )
	{
		super( a, nagents, base_repr );
		factored_repr_ = base_repr;
		abstract_repr_ = abstract_repr;
		lazy_threshold_ = lazy_threshold;
	}
	
	@Override
	public LazyAggregatingActionNode<S, A> create()
	{
		return new LazyAggregatingActionNode<S, A>(
			a(), nagents, factored_repr_.create(), abstract_repr_.create(), lazy_threshold_ );
	}
	
	private static <S, A extends VirtualConstructor<A>>
	void merge( final MutableStateNode<S, A> into, final MutableStateNode<S, A> from )
	{
		// Reconcile statistics
		for( int i = 0; i < into.rv_.length; ++i ) {
			into.rv_[i].combine( from.rv_[i] );
		}
		into.n_ += from.n_;
		assert( into.rv_[0].n() == into.n_ );
		
		// Recursively merge any equal children
		for( final MutableActionNode<S, A> a_from : Fn.in( from.successors() ) ) {
			final MutableActionNode<S, A> a_into = into.getActionNode( a_from.a() );
			if( a_into != null ) {
				merge( a_into, a_from );
			}
			else {
				into.a_.put( a_from.a(), a_from );
			}
		}
	}
	
	private static <S, A extends VirtualConstructor<A>>
	void merge( final MutableActionNode<S, A> into, final MutableActionNode<S, A> from )
	{
		// Reconcile statistics
		for( int i = 0; i < into.rv_.length; ++i ) {
			into.rv_[i].combine( from.rv_[i] );
		}
		for( int i = 0; i < into.qv_.length; ++i ) {
			into.qv_[i].combine( from.qv_[i] );
		}
		into.n_ += from.n_;
		
		if( into.rv_[0].n() != into.n_ ) {
			System.out.println( into );
			System.out.println( from );
			System.out.println( "! into.rv_[0].n() = " + into.rv_[0].n() );
			System.out.println( "! into.n_ = " + into.n_ );
			assert( into.rv_[0].n() == into.n_ );
		}
		
		
		// Recursively merge any equal children
		for( final Map.Entry<StateTuple<S>, MutableStateNode<S, A>> e : from.s_.entrySet() ) {
			final StateTuple<S> st = e.getKey();
			final MutableStateNode<S, A> s_into = into.getStateNode( st.x, st.turn );
			if( s_into != null ) {
				merge( s_into, e.getValue() );
			}
			else {
				into.s_.put( e.getKey(), e.getValue() );
			}
		}
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
				leaf = new LeafStateNode<S, A>( x, nagents, turn );
				attachSuccessor( x, turn, leaf );
			}
			
			return leaf;
		}
		
		final Representation<S> r;
		if( this.n() >= lazy_threshold_ ) {
			// Use abstraction
			if( lazy_ ) {
				// Stop being lazy
//				System.out.println( "! Up and at them!" );
				final ArrayList<Map.Entry<StateTuple<S>, MutableStateNode<S, A>>> children
					= Fn.takeAll( s_.entrySet().iterator() );
				s_.clear();
				
				for( final Map.Entry<StateTuple<S>, MutableStateNode<S, A>> e : children ) {
					if( e.getValue() instanceof LeafStateNode ) {
						continue;
					}
					// This cast is safe because all of these keys were encoded with 'factored_repr_'
					final FactoredRepresentation<S> x = (FactoredRepresentation<S>) e.getKey().x;
					final Representation<S> ab = abstract_repr_.encode( x );
					final MutableStateNode<S, A> sn = getStateNode( ab, turn );
					if( sn != null ) {
						final MutableStateNode<S, A> to_merge = e.getValue();
						merge( sn, to_merge );
					}
					else {
						s_.put( new StateTuple<S>( ab, e.getKey().turn ), e.getValue() );
					}
				}
				
				lazy_ = false;
			}
			
			final FactoredRepresentation<S> x = factored_repr_.encode( s );
			r = abstract_repr_.encode( x );
		}
		else {
			// Don't use abstraction
			r = factored_repr_.encode( s );
		}
		
		final MutableStateNode<S, A> sn = getStateNode( r, turn );
		if( sn != null ) {
//			System.out.println( "Hit " + x );
			return sn;
		}
		else {
//			System.out.println( "Miss " + x );
			final MutableStateNode<S, A> succ = createSuccessor( s, r, nagents, turn, action_gen );
			attachSuccessor( r, turn, succ );
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
				return new LazyAggregatingActionNode<S, A>(
					a, nagents, factored_repr_.create(), abstract_repr_.create(), lazy_threshold_ );
			}
		};
	}
}
