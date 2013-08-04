/**
 * 
 */
package edu.oregonstate.eecs.mcplan.graph;

import edu.oregonstate.eecs.mcplan.util.Generator;


/**
 * @author jhostetler
 *
 */
public interface IncidenceGraph<V, E> extends Graph<V, E>
{
	public abstract V source( final E e );
	public abstract V target( final E e );
	
	public abstract Generator<E> outEdges( final V v );
	public abstract int outDegree( final V v );
}
