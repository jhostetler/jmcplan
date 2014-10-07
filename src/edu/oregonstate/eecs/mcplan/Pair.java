/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author jhostetler
 *
 */
public final class Pair<T, U>
{
	public static class PartialComparator<T extends Comparable<T>, U>
		implements java.util.Comparator<Pair<T, U>>
	{
		@Override
		public int compare( final Pair<T, U> a, final Pair<T, U> b )
		{
			return a.first.compareTo( b.first );
		}
	}
	
	public static class Comparator<T extends Comparable<T>, U extends Comparable<U>>
		implements java.util.Comparator<Pair<T, U>>
	{
		@Override
		public int compare( final Pair<T, U> a, final Pair<T, U> b )
		{
			final int ac = a.first.compareTo( b.first );
			if( ac == 0 ) {
				return a.second.compareTo( b.second );
			}
			else {
				return ac;
			}
		}
	}
	
	public final T first;
	public final U second;
	
	public Pair( final T t, final U u )
	{
		first = t;
		second = u;
	}
	
	public static <T, U> Pair<T, U> makePair( final T t, final U u )
	{
		return new Pair<T, U>( t, u );
	}
	
	@Override
	public int hashCode()
	{
		final HashCodeBuilder hb = new HashCodeBuilder();
		hb.append( first );
		hb.append( second );
		return hb.toHashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof Pair<?, ?>) ) {
			return false;
		}
		
		final Pair<T, U> that = (Pair<T, U>) obj;
		return ((first == null && that.first == null) || first.equals( that.first ))
				&& ((second == null && that.second == null) || second.equals( that.second ));
	}
	
	@Override
	public String toString()
	{
		return "{" + first + ", " + second + "}";
	}
}
