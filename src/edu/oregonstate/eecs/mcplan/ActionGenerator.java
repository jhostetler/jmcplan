/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.Iterator;

/**
 * An iterator-like type that generates the legal actions in a state.
 * 
 * The implementation must *not* require initialization specific to a
 * particular state or player. That's what the parameters in setState() are
 * for.
 */
public interface ActionGenerator<S, A> extends Iterator<A>
{
	/**
	 * Create an independent, identically-initialized instance of this class.
	 * The new instance must start listing from the beginning, *not* from
	 * the point that the instance being copied is at.
	 * @return
	 */
	public abstract ActionGenerator<S, A> create();
	
	/**
	 * Inherited from Iterator<A>. Must throw UnsupportedOperationException.
	 * @param a
	 */
	@Override
	public abstract void remove();
	
	public abstract void setState( final S s, final long t, final int turn );
	
	public abstract int size();
}
