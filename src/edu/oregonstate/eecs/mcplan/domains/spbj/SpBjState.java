/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.spbj;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.domains.cards.Card;
import edu.oregonstate.eecs.mcplan.domains.cards.Deck;

/**
 * @author jhostetler
 *
 */
public class SpBjState implements State
{
	public final SpBjHand player_hand;
	public final ArrayList<Card> dealer_hand;
	
	private final Deck deck;
	
	public int r = 0;
	
	public SpBjState( final Deck deck )
	{
		this.deck = deck;
		player_hand = new SpBjHand();
		dealer_hand = new ArrayList<Card>();
	}
	
	private SpBjState( final SpBjState s )
	{
		player_hand = s.player_hand.copy();
		dealer_hand = new ArrayList<Card>();
		dealer_hand.addAll( s.dealer_hand );
		deck = s.deck.copy();
	}
	
	public SpBjState copy()
	{
		return new SpBjState( this );
	}
	
	public void init()
	{
		dealer_hand.add( deck.deal() );
		
		final ArrayList<Card> player_cards = new ArrayList<Card>();
		player_cards.add( deck.deal() );
		player_cards.add( deck.deal() );
		player_hand.bets[0] = SpBjHand.min_bet;
		player_hand.hands.add( player_cards );
		player_hand.Nhands = 1;
		player_hand.passed[0] = false;
	}
	
	public Deck deck()
	{
		return deck;
	}
	
	public Card dealerUpcard()
	{
		return dealer_hand.get( 0 );
	}
	
	public ArrayList<Card> dealerHand()
	{
		return dealer_hand;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "d: " ).append( dealer_hand );
		sb.append( ", " ).append( "p: " ).append( player_hand );
		sb.append( ", " ).append( "r: " ).append( r );
		return sb.toString();
	}

	@Override
	public boolean isTerminal()
	{
		for( final boolean b : player_hand.passed ) {
			if( !b ) {
				return false;
			}
		}
		return true;
	}
}
