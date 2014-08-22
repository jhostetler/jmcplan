/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * A function that represents the set S with the set X. The 'Self' type
 * parameter must be the type implementing Representer.
 */
public interface Representer<T, U extends Representation<?>>
{
	public abstract Representer<T, U> create();
	public abstract U encode( final T s );
}
