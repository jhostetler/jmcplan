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
				sb.append( itr.next() );
				if( i++ > 0 ) {
					sb.append( "; " );
				}
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
