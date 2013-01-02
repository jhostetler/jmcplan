/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

/**
 * @author jhostetler
 *
 */
public interface Function<R, A>
{
	public abstract R apply( final A a );
}
