/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
