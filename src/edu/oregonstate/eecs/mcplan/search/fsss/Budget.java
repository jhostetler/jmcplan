/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

/**
 * @author jhostetler
 *
 */
public interface Budget
{
	public abstract boolean isExceeded();
	public double actualDouble();
	public abstract void reset();
}
