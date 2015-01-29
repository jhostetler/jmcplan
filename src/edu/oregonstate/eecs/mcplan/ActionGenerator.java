/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * A Generator that yields the legal actions in a state.
 * 
 * Actions *must* be returned in a consistent order.
 * 
 * The implementation must *not* require initialization specific to a
 * particular state or player. That's what the parameters in setState() are
 * for.
 */
public abstract class ActionGenerator<S, A> extends Generator<A>
{
	/**
	 * Create an independent, identically-initialized instance of this class.
	 * The new instance must start listing from the beginning, *not* from
	 * the point that the instance being copied is at.
	 * @return
	 */
	public abstract ActionGenerator<S, A> create();
	
	public abstract void setState( final S s, final long t );
	
	public abstract int size();
	
//	public abstract void repeat();
	
	// FIXME: Should require hashCode() / equals()
	
	@Override
	public String toString()
	{
		return this.getClass().getName();
	}
}
