/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;

/**
 * @author jhostetler
 *
 */
public interface LongHashFunction<T>
{
	public abstract long hash( final T t );
}
