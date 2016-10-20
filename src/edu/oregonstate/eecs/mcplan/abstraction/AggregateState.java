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

package edu.oregonstate.eecs.mcplan.abstraction;

import java.util.ArrayList;
import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.Representation;

/**
 * An AggregateState consists of a set of primitive states. It has
 * *reference semantics*, which is different from typical usage for
 * Representation types. We can get away with this because AggregateState
 * instances are only created by the Aggregator class.
 */
public class AggregateState<S> extends Representation<S> implements Iterable<Representation<S>>
{
	private final ArrayList<Representation<S>> xs_
		= new ArrayList<Representation<S>>();
	
//		private final HashCodeBuilder hash_builder_ = new HashCodeBuilder( 139, 149 );
	
	public <R extends Representation<S>> void add( final R x )
	{
		xs_.add( x );
//			hash_builder_.append( x );
	}
	
	@Override
	public AggregateState<S> copy()
	{
//			System.out.println( "AggregateState.copy()" );
		final AggregateState<S> cp = new AggregateState<S>();
		for( final Representation<S> x : xs_ ) {
			cp.add( x );
		}
		return cp;
	}

	@Override
	public boolean equals( final Object obj )
	{
//			if( obj == null || !(obj instanceof AggregateState) ) {
//				return false;
//			}
//			final AggregateState that = (AggregateState) obj;
//			if( xs_.size() != that.xs_.size() ) {
//				return false;
//			}
//			for( int i = 0; i < xs_.size(); ++i ) {
//				if( !xs_.get( i ).equals( that.xs_.get( i ) ) ) {
//					return false;
//				}
//			}
//			return true;
		return this == obj;
	}

	@Override
	public int hashCode()
	{
//			return hash_builder_.toHashCode();
		return System.identityHashCode( this );
	}

	@Override
	public Iterator<Representation<S>> iterator()
	{
		return xs_.iterator();
	}
}