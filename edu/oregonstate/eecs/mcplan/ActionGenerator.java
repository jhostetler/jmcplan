/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.Iterator;

/**
 * @author jhostetler
 *
 */
public interface ActionGenerator<S, A> extends Iterator<A>
{
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
