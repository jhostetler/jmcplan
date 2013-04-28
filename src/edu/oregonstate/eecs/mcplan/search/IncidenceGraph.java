/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.List;

/**
 * @author jhostetler
 *
 */
public interface IncidenceGraph<V, E> extends Graph<V, E>
{
	public abstract V source( final E e );
	public abstract V target( final E e );
	
	public abstract List<E> outEdges( final V v );
	public abstract int outDegree( final V v );
}
