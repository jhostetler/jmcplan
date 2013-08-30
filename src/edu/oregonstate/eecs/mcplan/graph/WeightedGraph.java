/**
 * 
 */
package edu.oregonstate.eecs.mcplan.graph;

/**
 * @author jhostetler
 *
 */
public interface WeightedGraph<V, E>
{
	public abstract double weight( final E e );
}
