/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.pats;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class ActionSet<A> implements Set<A>
{
	private final Set<A> set = new LinkedHashSet<A>();
	public final A default_action;
	
	public ActionSet( final A default_action )
	{
		this.default_action = default_action;
		add( default_action );
	}
	
	@Override
	public Iterator<A> iterator()
	{
		final Iterator<A> itr = set.iterator();
		return Generator.fromIterator( itr );
	}

	@Override
	public int size()
	{
		return set.size();
	}

	@Override
	public boolean add( final A e )
	{
		return set.add( e );
	}

	@Override
	public boolean addAll( final Collection<? extends A> c )
	{
		return set.addAll( c );
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains( final Object o )
	{
		return set.contains( o );
	}

	@Override
	public boolean containsAll( final Collection<?> c )
	{
		return set.containsAll( c );
	}

	@Override
	public boolean isEmpty()
	{
		assert( !set.isEmpty() );
		return false;
	}

	@Override
	public boolean remove( final Object o )
	{
		// TODO: We may want to support this operation later.
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll( final Collection<?> c )
	{
		// This is never OK because we always need at least one action.
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll( final Collection<?> c )
	{
		// Wouldn't be hard to support, but I don't have a use for it right now.
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray()
	{
		return set.toArray();
	}

	@Override
	public <T> T[] toArray( final T[] a )
	{
		return set.toArray( a );
	}
	
}
