/**
 * 
 */
package edu.oregonstate.eecs.mcplan.agents.galcon;

/**
 * @author jhostetler
 *
 */
public interface Action<S, A>
{
	public abstract void doAction( final S s );
	public abstract boolean isDone();
	public abstract A create();
}
