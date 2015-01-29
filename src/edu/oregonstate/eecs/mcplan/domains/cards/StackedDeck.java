/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cards;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author jhostetler
 *
 */
public class StackedDeck implements Deck
{
	private final Deque<Card> original_cards;
	private final Deque<Card> cards;
	
	public StackedDeck( final Deque<Card> cards )
	{
		this.cards = cards;
		this.original_cards = new ArrayDeque<Card>( cards );
	}
	
	@Override
	public Card deal()
	{
		return cards.pop();
	}

	@Override
	public void undeal( final Card c )
	{
		cards.push( c );
	}

	@Override
	public Deck copy()
	{
		return new StackedDeck( original_cards );
	}

}
