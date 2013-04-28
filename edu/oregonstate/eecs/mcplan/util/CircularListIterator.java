/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import java.util.List;
import java.util.ListIterator;

/**
 * A ListIterator that goes in a circle.
 * 
 * @author jhostetler
 */
public class CircularListIterator<T> implements ListIterator<T>
{
	private final List<T> list_;
	private ListIterator<T> itr_ = null;
	
	/**
	 * 
	 */
	public CircularListIterator( final List<T> list )
	{
		this( list, 0 );
	}
	
	public CircularListIterator( final List<T> list, final int idx )
	{
		list_ = list;
		itr_ = list_.listIterator( idx );
	}
	
	public CircularListIterator( final CircularListIterator<T> that )
	{
		list_ = that.list_;
		itr_ = list_.listIterator( that.nextIndex() );
	}

	@Override
	public boolean hasNext()
	{
		return !list_.isEmpty();
	}

	@Override
	public T next()
	{
		if( !itr_.hasNext() ) {
			itr_ = list_.listIterator();
		}
		// This will still throw for an empty list.
		return itr_.next();
	}

	@Override
	public boolean hasPrevious()
	{
		return !list_.isEmpty();
	}

	@Override
	public T previous()
	{
		if( !itr_.hasPrevious() ) {
			itr_ = list_.listIterator( list_.size() );
		}
		// This will throw for an empty list.
		return itr_.previous();
	}

	@Override
	public int nextIndex()
	{
		if( !itr_.hasNext() ) {
			return 0;
		}
		else {
			return itr_.nextIndex();
		}
	}

	@Override
	public int previousIndex()
	{
		if( !itr_.hasPrevious() ) {
			return list_.size() - 1;
		}
		else {
			return itr_.previousIndex();
		}
	}

	@Override
	public void remove()
	{
		itr_.remove();
	}

	@Override
	public void set( final T e )
	{
		itr_.set( e );
	}

	@Override
	public void add( final T e )
	{
		itr_.add( e );
	}
}
