/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;

import org.apache.commons.math3.linear.RealVector;

/**
 * @author jhostetler
 *
 */
public interface VectorSpaceEmbedding<T>
{
	public abstract RealVector transform( final T v );
//	public abstract int inDimension();
	public abstract int outDimension();
	public abstract String name();
}
