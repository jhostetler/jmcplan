/**
 * 
 */
package edu.oregonstate.eecs.mcplan.dp;

/**
 * @author jhostetler
 *
 */
public interface ValueFunction<S>
{
	public abstract double v( final S s );
}
