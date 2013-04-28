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
	@Override
	@Deprecated
	public final void remove()
	{
		throw new UnsupportedOperationException();
	}
}
