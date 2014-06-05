/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * @author jhostetler
 *
 */
public abstract class Policy<S, A>
{
	public abstract void setState( final S s, final long t );
	
	public abstract A getAction();
	
	/**
	 * This function may be called by the execution environment to provide
	 * reward feedback. The default implementation is a no-op.
	 * 
	 * We adopt the most general reward model and assume that the reward is a
	 * function of the entire transition (s, a, s') -> r. Thus the r given
	 * to actionResult() is the reward "in" s'.
	 * 
	 * @param sprime
	 * @param r
	 * @param s
	 */
	public abstract void actionResult( final S sprime, final double[] r );
	
	public abstract String getName();
	
	@Override
	public abstract int hashCode();
	
	@Override
	public abstract boolean equals( final Object that );
}
