/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cards;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public class InfiniteSpanishDeck implements Deck
{
	private final RandomGenerator rng;
	
	public InfiniteSpanishDeck( final RandomGenerator rng )
	{
		this.rng = rng;
	}
	
	@Override
	public Card deal()
	{
		while( true ) {
			final int i = rng.nextInt( 52 );
			final Card c = Card.values()[i];
			if( c.rank != Rank.R_T ) {
				return c;
			}
		}
	}
	
	@Override
	public void undeal( final Card c )
	{
		// Nothing to do
	}

	@Override
	public Deck copy()
	{
		// Since the deck is infinite, it has no meaningful state.
		return this;
	}
}
