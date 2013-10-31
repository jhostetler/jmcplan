/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * A function that represents the set S with the set X. The 'Self' type
 * parameter must be the type implementing Representer.
 */
public interface Representer<S, X extends Representation<S>>
{
	public abstract Representer<S, X> create();
	public abstract X encode( final S s );
}
