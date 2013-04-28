/**
 * 
 */
package edu.oregonstate.eecs.mcplan;



/**
 * @author jhostetler
 *
 */
public interface AnytimePolicy<S, A> extends Policy<S, A>
{
	public abstract long minControl();
	public abstract long maxControl();
	public abstract A getAction( final long control );
}
