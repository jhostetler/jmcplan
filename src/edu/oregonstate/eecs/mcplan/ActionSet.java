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
