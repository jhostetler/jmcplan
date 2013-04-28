/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * @author jhostetler
 *
 */
public interface Policy<S, A>
{
	public abstract void setState( final S s, final long t );
	
	public abstract A getAction();
	
	/**
	 * This function may be called by the execution environment to provide
	 * reward feedback. The default implementation is a no-op.
	 * @param s
	 * @param a
	 * @param sprime
	 * @param r
	 */
	public abstract void actionResult( final A a, final S sprime, final double r );
	
	public abstract String getName();
	
	@Override
	public abstract int hashCode();
	
	@Override
	public abstract boolean equals( final Object that );
}
