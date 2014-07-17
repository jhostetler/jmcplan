/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;


/**
 * @author jhostetler
 *
 */
public interface KernelFunction<T>
{
	public double apply( final T x, final T y );
}
