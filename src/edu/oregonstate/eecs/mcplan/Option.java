/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * Represents an option, which is a policy combined with a termination
 * condition.
 * <p>
 * A policy over options must have a non-zero probability of choosing an
 * Option that does not terminate with probability 1 in its initial state.
 * Otherwise, infinite loops may occur.
 */
public abstract class Option<S, A> implements VirtualConstructor<Option<S, A>>
{
	public abstract void start( final S s, final long t );
	public abstract double terminate( final S s, final long t );
	public abstract Policy<S, A> pi();
}
