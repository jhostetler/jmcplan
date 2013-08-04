/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import java.util.Iterator;

/**
 * An object that can provide a (potentially infinite) stream of objects.
 * 
 * It implements Iterator for compatibility with Collections, but
 * remove() is overriden to throw an exception.
 * 
 * @author jhostetler
 */
public abstract class Generator<T> implements Iterator<T>
{
	public static <T> Generator<T> fromIterator( final Iterator<T> itr )
	{
		return new Generator<T>() {
			@Override
			public boolean hasNext() { return itr.hasNext(); }
			@Override
			public T next() { return itr.next(); }
		};
	}
	
	@Override
	public abstract boolean hasNext();
	
	@Override
	public abstract T next();
	
	@Override
	@Deprecated
	public final void remove()
	{
		throw new UnsupportedOperationException();
	}
}
