/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import edu.oregonstate.eecs.mcplan.Representer;

/**
 * @author jhostetler
 *
 */
public class BlackjackAggregator implements Representer<BlackjackState, HandValueAbstraction>
{

	@Override
	public Representer<BlackjackState, HandValueAbstraction> create()
	{
		return new BlackjackAggregator();
	}

	@Override
	public HandValueAbstraction encode( final BlackjackState s )
	{
		return new HandValueAbstraction( s );
	}

	@Override
	public String toString()
	{
		return "value";
	}
}
