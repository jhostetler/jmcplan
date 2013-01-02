/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

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
}
