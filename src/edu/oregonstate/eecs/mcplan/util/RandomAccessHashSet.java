/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * Provides constant-complexity membership queries (like Set) as well as
 * constant-time random access (like ArrayList). Many of the functions,
 * specifically _mutate_All and set( int, T ) throw
 * UnsupportedOperationException because their proper semantics are unclear
 * and I don't need them right now.
 * 
 * @author jhostetler
 */
public class RandomAccessHashSet<T> implements Set<T>, List<T>
{
	private final ArrayList<T> list = new ArrayList<T>();
	private final Set<T> set = new HashSet<T>();
	
	@Override
	public void add( final int i, final T x )
	{
		if( set.add( x ) ) {
			list.add( i, x );
		}
	}
	
	@Override
	public boolean addAll( final int arg0, final Collection<? extends T> arg1 )
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public T get( final int i )
	{
		return list.get( i );
	}
	
	@Override
	public int indexOf( final Object x )
	{
		return list.indexOf( x );
	}
	
	@Override
	public int lastIndexOf( final Object x )
	{
		return list.lastIndexOf( x );
	}
	
	@Override
	public ListIterator<T> listIterator()
	{
		return list.listIterator();
	}
	
	@Override
	public ListIterator<T> listIterator( final int i )
	{
		return list.listIterator( i );
	}
	
	@Override
	public T remove( final int i )
	{
		final T x = list.remove( i );
		final boolean check = set.remove( x );
		assert( check );
		return x;
	}
	
	@Override
	public T set( final int i, final T x )
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public RandomAccessHashSet<T> subList( final int start, final int end )
	{
		final RandomAccessHashSet<T> result = new RandomAccessHashSet<T>();
		for( int i = start; i < end; ++i ) {
			result.add( list.get( i ) );
		}
		return result;
	}
	
	@Override
	public boolean add( final T x )
	{
		final boolean b = set.add( x );
		if( b ) {
			list.add( x );
		}
		return b;
	}
	
	@Override
	public boolean addAll( final Collection<? extends T> c )
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void clear()
	{
		list.clear();
		set.clear();
	}
	
	@Override
	public boolean contains( final Object x )
	{
		return set.contains( x );
	}
	
	@Override
	public boolean containsAll( final Collection<?> c )
	{
		return set.containsAll( c );
	}
	
	@Override
	public boolean isEmpty()
	{
		return set.isEmpty();
	}
	
	@Override
	public Iterator<T> iterator()
	{
		return Generator.fromIterator( list.iterator() );
	}
	
	@Override
	public boolean remove( final Object x )
	{
		final boolean b = set.remove( x );
		if( b ) {
			final boolean check = list.remove( x );
			assert( check );
		}
		return b;
	}
	
	@Override
	public boolean removeAll( final Collection<?> c )
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean retainAll( final Collection<?> c )
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int size()
	{
		return set.size();
	}
	
	@Override
	public Object[] toArray()
	{
		return list.toArray();
	}
	
	@Override
	public <U> U[] toArray( final U[] a )
	{
		return list.toArray( a );
	}
}
