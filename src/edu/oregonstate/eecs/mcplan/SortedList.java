/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.AbstractQueue;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * A queue that stores elements in sorted order. Removing the first element
 * is O(1). Inserting is O(n) comparisons. This can be reduced to O(log n)
 * using binary search if that turns out to be necessary.
 * 
 * We require use of a Comparator instance for efficiency reasons, so that
 * we don't have to check whether the one has been supplied before every
 * operation.
 * 
 * Note that the need to maintain the ordering invariant means that operations
 * that certain operations are not implemented. Specifically, add(int, T)
 * and set(int, T) throw UnsupportedOperationException. remove(int) is also
 * not implemented, but only because I'm lazy.
 */
public class SortedList<T> extends AbstractQueue<T>
{
	private final LinkedList<T> list_ = new LinkedList<T>();
	private final Comparator<T> comp_;
	
	public SortedList( final Comparator<T> comp )
	{
		comp_ = comp;
	}
	
	@Override
	public int size()
	{
		return list_.size();
	}

	@Override
	public Iterator<T> iterator()
	{
		return list_.iterator();
	}

	@Override
	public boolean offer( final T t )
	{
		// TODO: This could use binary search; dubious benefit for Voyager.
		// Java wants me to use TreeSet, but it has O(log n) remove operations,
		// and I would prefer the O(1) remove we can get this way.
		if( t == null ) {
			throw new NullPointerException();
		}
		final ListIterator<T> itr = list_.listIterator();
		while( itr.hasNext() ) {
			final T candidate = itr.next();
			final int c = comp_.compare( candidate, t );
			if( c > 0 ) {
				itr.previous();
				itr.add( t );
				return true;
			}
		}
		itr.add( t );
		return true;
	}

	@Override
	public T peek()
	{
		return list_.peekFirst();
	}

	@Override
	public T poll()
	{
		return list_.pollFirst();
	}
}
