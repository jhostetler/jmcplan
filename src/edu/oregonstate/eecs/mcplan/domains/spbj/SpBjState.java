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
	
	@Override
	public void close()
	{ }
	
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
