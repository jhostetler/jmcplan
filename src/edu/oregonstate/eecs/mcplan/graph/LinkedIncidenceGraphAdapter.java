/**
 * 
 */
package edu.oregonstate.eecs.mcplan.graph;

import java.util.ListIterator;

/**
 * @author jhostetler
 *
 */
public abstract class LinkedIncidenceGraphAdapter<V, E> implements IncidenceGraph<V, E>
{

	@Override
	public V source( final E e )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V target( final E e )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListIterator<E> outEdges( final V v )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int outDegree( final V v )
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
