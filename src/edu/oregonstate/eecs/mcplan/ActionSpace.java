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
package edu.oregonstate.eecs.mcplan;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableSet;


/**
 * @author jhostetler
 *
 */
public abstract class ActionSpace<S, A> implements Representer<S, Representation<S>>
{
	public abstract ActionSet<S, A> getActionSet( final S s );
	
	/**
	 * FIXME: This is a temporary, intermediate step toward a larger interface
	 * change.
	 * @deprecated
	 * @see edu.oregonstate.eecs.mcplan.Representer#encode(java.lang.Object)
	 */
	@Deprecated
	@Override
	public final Representation<S> encode( final S s )
	{ return getActionSet( s ); }
	
	/**
	 * @deprecated
	 * @see edu.oregonstate.eecs.mcplan.ActionSpace#encode(Object)
	 * @see edu.oregonstate.eecs.mcplan.Representer#create()
	 */
	@Deprecated
	@Override
	public final ActionSpace<S, A> create()
	{ throw new UnsupportedOperationException(); }
	
	public abstract int cardinality();
	public abstract boolean isFinite();
	public abstract boolean isCountable();
	// FIXME: This could be more efficiently implemented as a field in the
	// action object, but then all actions would have to be created by an
	// ActionSpace.
	public abstract int index( final A a );
	
	// -----------------------------------------------------------------------
	
	public static <S, A> ActionSpace<S, A> union( final List<ActionSpace<S, A>> ass )
	{
		return new UnionActionSpace<>( ass );
	}
	
	private static class UnionActionSpace<S, A> extends ActionSpace<S, A>
	{
		private final List<ActionSpace<S, A>> ass;
		
		public UnionActionSpace( final List<ActionSpace<S, A>> ass )
		{
			this.ass = ass;
		}
		
		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder();
			sb.append( "UnionActionSpace[" );
			int i = 0;
			final Iterator<ActionSpace<S, A>> itr = ass.iterator();
			while( itr.hasNext() ) {
				if( i++ > 0 ) {
					sb.append( "; " );
				}
				sb.append( itr.next() );
			}
			sb.append( "]" );
			return sb.toString();
		}
		
		@Override
		public ActionSet<S, A> getActionSet( final S s )
		{
			final ImmutableSet.Builder<A> b = ImmutableSet.builder();
			for( final ActionSpace<S, A> as : ass ) {
				b.addAll( as.getActionSet( s ) );
			}
			return ActionSet.constant( b.build() );
		}

		@Override
		public int cardinality()
		{
			int card = 0;
			for( final ActionSpace<S, A> as : ass ) {
				card += as.cardinality();
			}
			return card;
		}

		@Override
		public boolean isFinite()
		{
			for( final ActionSpace<S, A> as : ass ) {
				if( !as.isFinite() ) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean isCountable()
		{
			for( final ActionSpace<S, A> as : ass ) {
				if( !as.isCountable() ) {
					return false;
				}
			}
			return true;
		}

		@Override
		public int index( final A a )
		{
			throw new UnsupportedOperationException();
		}
		
	}
}
