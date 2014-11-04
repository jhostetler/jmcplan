/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2.subtask;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.Hand;

/**
 * @author jhostetler
 *
 */
public class YahtzeeDiceState implements State
{
	public final Hand hand;
	public final int rerolls;
	
	public YahtzeeDiceState( final Hand hand, final int rerolls )
	{
		this.hand = hand;
		this.rerolls = rerolls;
	}
	
	@Override
	public boolean isTerminal()
	{
		return rerolls == 0;
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		final YahtzeeDiceState that = (YahtzeeDiceState) obj;
		return this.hand.equals( that.hand )
			   && this.rerolls == that.rerolls;
	}
	
	@Override
	public int hashCode()
	{
		return 3 + 5 * (hand.hashCode() + 7 * rerolls);
	}
	
	@Override
	public String toString()
	{
		return "{rerolls: " + rerolls + ", hand: " + hand + "}";
	}
}
