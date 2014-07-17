/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public abstract class ActionSpace<S, A>
{
	public abstract void setState( final S s );
	
	public abstract int cardinality();
	public abstract boolean isFinite();
	public abstract boolean isCountable();
	// FIXME: This could be more efficiently implemented as a field in the
	// action object, but then all actions would have to be created by an
	// ActionSpace.
	public abstract int index( final A a );
	
	public abstract Generator<A> generator();
}
