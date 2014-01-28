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
	
	public abstract Generator<A> generator();
}
