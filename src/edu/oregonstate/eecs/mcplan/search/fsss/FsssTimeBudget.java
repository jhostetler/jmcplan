/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * @author jhostetler
 *
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
//			System.out.println( "!\t Already exceeded: " + exceeded_at );
			return true;
		}
		final long now = bean.getCurrentThreadUserTime(); //System.nanoTime();
		final long interval = (now - start_ns);
		final boolean b = interval > budget_ns;
		if( b ) {
//			System.out.println( "! Shot clock: " + now + " - " + start_ns + " = " + interval );
			exceeded = true;
			exceeded_at = interval;
		}
		return b;
	}

	@Override
	public void reset()
	{
//		System.out.println( "! Reset" );
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
