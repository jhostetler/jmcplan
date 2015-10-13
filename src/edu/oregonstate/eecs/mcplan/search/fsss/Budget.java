/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

/**
 * Represents an abstract, exhaustible resource budget.
 * <p>
 * Compliance with budget constraints is on a best-effort basis. It is not
 * normally practical to stop *exactly* when the budget is exceeded.
 */
public interface Budget
{
	/**
	 * @return True if the budget has been exhausted.
	 */
	public abstract boolean isExceeded();
	
	/**
	 * If isExceeded() == true, returns a double representing how much of
	 * the budget resource was actually used.
	 * @return A double representation of budget used.
	 */
	public double actualDouble();
	
	/**
	 * Reset budget usage to 0.
	 */
	public abstract void reset();
}
