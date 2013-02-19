package edu.oregonstate.eecs.mcplan.search;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An Iterator that yields an element supplied at construction-time first,
 * and then all elements returned by a base iterator that are *not*
 * equal to the special element (using equals()).
 * @author jhostetler
 *
 * @param <T>
 */
public class DistinguishedElementIterator<T> implements Iterator<T>
{
	private final Iterator<T> base_;
	private final T special_element_;
	private boolean used_ = false;
	
	private T next_ = null;
	private boolean has_next_ = false;
	
	public DistinguishedElementIterator( final Iterator<T> base, final T special_element )
	{
		base_ = base;
		assert( special_element != null );
		special_element_ = special_element;
		
		advance();
	}
	
	private void advance()
	{
		has_next_ = false;
		while( base_.hasNext() ) {
			next_ = base_.next();
			if( !special_element_.equals( next_ ) ) {
				has_next_ = true;
				break;
			}
		}
	}
	
	@Override
	public boolean hasNext()
	{
		if( !used_ ) {
			return true;
		}
		else {
			return has_next_;
		}
	}

	@Override
	public T next()
	{
		if( !used_ ) {
			used_ = true;
			return special_element_;
		}
		else if( has_next_ ) {
			advance();
			return next_;
		}
		else {
			throw new NoSuchElementException();
		}
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

}
