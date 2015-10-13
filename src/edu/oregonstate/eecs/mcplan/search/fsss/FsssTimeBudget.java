/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Budget in terms of CPU time.
 * <p>
 * The budget accounts for CPU usage by the *current thread only*. This means
 * that JVM system tasks like garbage collection don't count against the
 * time budget.
 * <p>
 * This budget adds a significant amount to the execution time, due to the
 * mechnism used to retrieve current thread time.
 */
public class FsssTimeBudget implements Budget
{
	private final ThreadMXBean bean;
	private final long budget_ns;
	private long start_ns = 0L;
	
	private boolean exceeded = false;
	private long exceeded_at = 0L;
	
	private final double ns_per_ms = 1000000.0;
	
	
	public FsssTimeBudget( final double budget_milliseconds )
	{
		bean = ManagementFactory.getThreadMXBean();
		final long one_million = 1000000L;
		this.budget_ns = (long) (one_million * budget_milliseconds);
	}
	
	@Override
	public boolean isExceeded()
	{
		if( exceeded ) {
			return true;
		}
		final long now = bean.getCurrentThreadUserTime(); //System.nanoTime();
		final long interval = (now - start_ns);
		final boolean b = interval > budget_ns;
		if( b ) {
			exceeded = true;
			exceeded_at = interval;
		}
		return b;
	}

	@Override
	public void reset()
	{
		start_ns = bean.getCurrentThreadUserTime(); //System.nanoTime();
		
		exceeded = false;
		exceeded_at = 0L;
	}
	
	@Override
	public String toString()
	{
		return "TimeBudget[" + (budget_ns / ns_per_ms) + " ms]";
	}

	@Override
	public double actualDouble()
	{
		return exceeded_at / ns_per_ms;
	}
}
