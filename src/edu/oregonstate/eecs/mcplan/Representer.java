/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * A function that represents the set S with the set X. The 'Self' type
 * parameter must be the type implementing Representer.
 */
public interface Representer<S, F extends Representer<S, F>>
{
	public abstract Representation<S, F> encode( final S s );
}
