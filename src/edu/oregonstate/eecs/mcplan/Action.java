/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * @author jhostetler
 *
 */
public interface Action<S>
{
	public abstract void doAction( final S s );
}
