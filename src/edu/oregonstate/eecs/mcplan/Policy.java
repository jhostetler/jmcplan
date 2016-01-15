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
	/**
	 * Called when a new "episode" is about to begin.
	 * <p>
	 * A non-stationary policy can use this as an opportunity to start
	 * recording a new history. Default implementation does nothing.
	 */
	public void reset()
	{ }
	
	/**
	 * Must return false if the policy exploits stored history information to
	 * choose an action.
	 * <p>
	 * Generally, if you've overridden reset(), you should override
	 * isStationary() to return false.
	 * @return
	 */
	public boolean isStationary()
	{ return true; }
	
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
	 * @deprecated This should be part of a separate "policy learner" interface.
	 * 
	 * @param sprime
	 * @param r
	 * @param s
	 */
	@Deprecated
	public abstract void actionResult( final S sprime, final double[] r );
	
	public abstract String getName();
	
	@Override
	public abstract int hashCode();
	
	@Override
	public abstract boolean equals( final Object that );
}
