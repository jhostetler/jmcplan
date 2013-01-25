/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

/**
 * @author jhostetler
 *
 */
public final class Countdown
{
	private final long duration_;
	private long remaining_;
	
	public Countdown( final long duration )
	{
		duration_ = duration;
		remaining_ = duration_;
	}
	
	public void count( final long ms )
	{
		remaining_ -= ms;
	}
	
	public boolean expired()
	{
		return remaining_ <= 0;
	}
}
