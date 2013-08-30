/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * @author jhostetler
 *
 */
public interface ZobristHash<T>
{
	public abstract long hash( final T t );
}
