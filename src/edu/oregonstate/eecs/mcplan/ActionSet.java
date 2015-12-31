/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.Collection;
import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.util.Generator;


/**
 * @author jhostetler
 *
 */
public abstract class ActionSet<S, A> extends Representation<S> implements Iterable<A>
{
	public abstract int size();
	
	// -----------------------------------------------------------------------
	
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
