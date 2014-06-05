/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public final class InfiniteDeck implements Deck
{
	private final RandomGenerator rng_;
	
	public InfiniteDeck()
	{
		rng_ = new MersenneTwister();
	}
	
	public InfiniteDeck( final int seed )
	{
		rng_ = new MersenneTwister( seed );
	}
	
	@Override
	public Card deal()
	{
		final int i = rng_.nextInt( 52 );
		return Card.values()[i];
	}
	
	@Override
	public void undeal( final Card c )
	{
		// Nothing to do
	}
}
