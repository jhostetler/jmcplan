/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * @author jhostetler
 *
 */
public interface State extends AutoCloseable
{
	public boolean isTerminal();
	
	/**
	 * Override to eliminate throws-declaration.
	 */
	@Override
	public void close();
}
