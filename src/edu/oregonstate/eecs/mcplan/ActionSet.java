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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.oregonstate.eecs.mcplan.util.Generator;


/**
 * @author jhostetler
 *
 */
public abstract class ActionSet<S, A> extends Representation<S> implements Iterable<A>
{
	public abstract int size();
	
	// -----------------------------------------------------------------------
	
	@SafeVarargs
	public static <S, A> ActionSet<S, A> union( final ActionSet<S, A>... ass )
	{
		return new UnionActionSet<>( ass );
	}
	
	public static <S, A> ActionSet<S, A> constant( final Iterable<A> as )
	{
		return new UnionActionSet<>( ImmutableSet.copyOf( as ) );
	}
	
	private static final class UnionActionSet<S, A> extends ActionSet<S, A>
	{
		private final Set<A> unique;
		
		@SafeVarargs
		public UnionActionSet( final Iterable<A>... ass )
		{
			final ImmutableSet.Builder<A> b = ImmutableSet.builder();
			for( final Iterable<A> as : ass ) {
				b.addAll( as );
			}
			unique = b.build();
		}
		
		public UnionActionSet( final Set<A> as )
		{
			unique = as;
		}
		
		@Override
		public Iterator<A> iterator()
		{
			return unique.iterator();
		}

		@Override
		public int size()
		{
			return unique.size();
		}

		@Override
		public Representation<S> copy()
		{
			return new UnionActionSet<S, A>( unique );
		}

		@Override
		public boolean equals( final Object obj )
		{
			if( !(obj instanceof ActionSet<?, ?>) ) {
				return false;
			}
			@SuppressWarnings( "unchecked" )
			final ActionSet<S, A> that = (ActionSet<S, A>) obj;
			final Set<A> that_unique = ImmutableSet.copyOf( that );
			return unique.equals( that_unique );
		}

		@Override
		public int hashCode()
		{
			return getClass().hashCode() ^ unique.hashCode();
		}
	}
	
	private static final class Wrapper<S, A> extends ActionSet<S, A>
	{
		private final Collection<A> actions;
		
		/*package*/ Wrapper( final Collection<A> actions )
		{
			this.actions = actions;
		}
		
		@Override
		public Iterator<A> iterator()
		{ return Generator.fromIterator( actions.iterator() ); }

		@Override
		public Representation<S> copy()
		{ return new Wrapper<>( actions ); }

		@Override
		public boolean equals( final Object obj )
		{
			@SuppressWarnings( "unchecked" )
			final Wrapper<S, A> that = (Wrapper<S, A>) obj;
			return actions.equals( that.actions );
		}

		@Override
		public int hashCode()
		{ return actions.hashCode(); }

		@Override
		public int size()
		{ return actions.size(); }
	}
	
	public static final <S, A> ActionSet<S, A> wrap( final Collection<A> actions )
	{
		return new Wrapper<>( actions );
	}
}
