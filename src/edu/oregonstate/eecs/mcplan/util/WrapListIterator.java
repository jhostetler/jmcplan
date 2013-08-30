/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * An iterator that returns all of the elements in a list, beginning at a
 * specified index and wrapping around to the beginning after reaching the
 * end. Unlike CircularListIterator, each element is returned exactly once.
 */
public class WrapListIterator<T> implements ListIterator<T>
{
	private final List<T> list_;
	private ListIterator<T> itr_ = null;
	private final int idx_;
	private boolean wrap_ = false;
	
	public WrapListIterator( final List<T> list )
	{
		this( list, 0 );
	}
	
	public WrapListIterator( final List<T> list, final int idx )
	{
		list_ = list;
		itr_ = list_.listIterator( idx );
		idx_ = idx;
	}
	
	public WrapListIterator( final WrapListIterator<T> that )
	{
		list_ = that.list_;
		itr_ = list_.listIterator( that.nextIndex() );
		idx_ = that.idx_;
	}

	@Override
	public boolean hasNext()
	{
		return nextIndex() != list_.size();
	}

	@Override
	public T next()
	{
		if( !itr_.hasNext() ) {
			itr_ = list_.listIterator();
		}
		if( !hasNext() ) {
			throw new NoSuchElementException();
		}
		if( itr_.nextIndex() == idx_ ) {
			wrap_ = true;
		}
		// This will still throw for an empty list.
		return itr_.next();
	}

	@Override
	public boolean hasPrevious()
	{
		return previousIndex() != -1;
	}

	@Override
	public T previous()
	{
		if( !itr_.hasPrevious() ) {
			itr_ = list_.listIterator( list_.size() );
		}
		if( !hasPrevious() ) {
			throw new NoSuchElementException();
		}
		if( itr_.previousIndex() == idx_ ) {
			wrap_ = false;
		}
		// This will throw for an empty list.
		return itr_.previous();
	}

	@Override
	public int nextIndex()
	{
		final int i;
		if( !itr_.hasNext() ) {
			i = 0;
		}
		else {
			i = itr_.nextIndex();
		}
		
		if( i == idx_ && wrap_ ) {
			return list_.size();
		}
		else {
			return i;
		}
	}

	@Override
	public int previousIndex()
	{
		final int i;
		if( !itr_.hasPrevious() ) {
			i = list_.size() - 1;
		}
		else {
			i = itr_.previousIndex();
		}
		
		if( i == idx_ - 1 && !wrap_ ) {
			return -1;
		}
		else {
			return i;
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
	
	public static void main( final String[] args )
	{
		final ArrayList<Integer> list = new ArrayList<Integer>();
		Collections.addAll( list, 0, 1, 2, 3, 4, 5 );
		WrapListIterator<Integer> itr = new WrapListIterator<Integer>( list );
		System.out.print( "[" );
		while( itr.hasNext() ) {
			System.out.print( itr.next() + "," );
		}
		System.out.println( "]" );
		itr = new WrapListIterator<Integer>( list, 3 );
		System.out.print( "[" );
		while( itr.hasNext() ) {
			System.out.print( itr.next() + "," );
		}
		System.out.println( "]" );
		itr = new WrapListIterator<Integer>( list, list.size() );
		System.out.print( "[" );
		while( itr.hasPrevious() ) {
			System.out.print( itr.previous() + "," );
		}
		System.out.println( "]" );
		itr = new WrapListIterator<Integer>( list, 3 );
		System.out.print( "[" );
		while( itr.hasPrevious() ) {
			System.out.print( itr.previous() + "," );
		}
		System.out.println( "]" );
		itr = new WrapListIterator<Integer>( list, 3 );
		System.out.print( "[" );
		while( itr.hasNext() ) {
			System.out.print( itr.next() + "," );
		}
		while( itr.hasPrevious() ) {
			System.out.print( itr.previous() + "," );
		}
		System.out.println( "]" );
	}
}
