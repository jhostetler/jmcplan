/**
 * 
 */
package edu.oregonstate.eecs.mcplan.graph;

import java.util.ListIterator;

/**
 * @author jhostetler
 *
 */
public interface Vertex<V, E>
{
	public abstract ListIterator<E> inEdges();
	public abstract ListIterator<E> outEdges();
}
