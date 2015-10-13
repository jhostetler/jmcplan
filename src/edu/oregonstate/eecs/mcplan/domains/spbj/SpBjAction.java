/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.spbj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.domains.cards.Card;
import edu.oregonstate.eecs.mcplan.domains.cards.Rank;
import edu.oregonstate.eecs.mcplan.domains.cards.Suit;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class SpBjAction implements UndoableAction<SpBjState>, VirtualConstructor<SpBjAction>
{
	public final SpBjActionCategory[] cat;
	
	private boolean done = false;
	private boolean terminal = false;
	
	public SpBjAction( final SpBjActionCategory[] cat )
	{
		this.cat = cat;
	}
	
	@Override
	public int hashCode()
	{
		return Arrays.hashCode( cat );
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		final SpBjAction that = (SpBjAction) obj;
		return Arrays.equals( cat, that.cat );
	}
	
	@Override
	public SpBjAction create()
	{
		final SpBjActionCategory[] copy = new SpBjActionCategory[cat.length];
		Fn.memcpy( copy, cat );
		return new SpBjAction( copy );
	}
	
	@Override
	public void doAction( final RandomGenerator rng, final SpBjState s )
	{
		assert( !done );
		
		for( int i = 0; i < cat.length; ++i ) {
			final SpBjHand h = s.player_hand;
			final ArrayList<Card> cards = h.hands.get( i );
			switch( cat[i] ) {
			case Hit:
				assert( !h.passed[i] );
				cards.add( s.deck().deal() );
				if( SpBjHand.handValue( cards )[0] >= SpBjHand.busted_score ) {
					h.passed[i] = true;
				}
				break;
			case Pass:
				h.passed[i] = true;
				break;
			case Split:
				assert( !h.passed[i] );
				assert( h.canSplit( i ) );
				final ArrayList<Card> old_hand = h.hands.get( i );
				assert( old_hand.size() == 2 );
				assert( old_hand.get( 0 ).rank == old_hand.get( 1 ).rank );
				final ArrayList<Card> new_hand = new ArrayList<Card>();
				final int j = h.Nhands;
				h.Nhands += 1;
				h.children[i] = j;
				h.bets[j] = h.bets[i];
				final Card c = old_hand.remove( 1 );
				new_hand.add( c );
				h.hands.add( new_hand );
				h.passed[j] = false;
				old_hand.add( s.deck().deal() );
				new_hand.add( s.deck().deal() );
				break;
			case Double:
				assert( !h.passed[i] );
				assert( h.canDouble( i ) );
				h.bets[i] *= 2;
				cards.add( s.deck().deal() );
				if( SpBjHand.handValue( cards )[0] >= SpBjHand.busted_score ) {
					h.passed[i] = true;
				}
				break;
			}
		}
		
		if( Fn.all( s.player_hand.passed ) ) {
			terminal = true;
			completeDealerHand( s );
		}
		
		done = true;
	}
	
	private void completeDealerHand( final SpBjState s )
	{
		final ArrayList<Card> hand = s.dealerHand();
		int[] dv = null;
		while( true ) {
			if( hand.size() < 2 ) {
				hand.add( s.deck().deal() );
			}
			else {
				dv = SpBjHand.handValue( hand );
				// Dealer stands on soft 17
				if( dv[0] <= SpBjHand.dealer_threshold ) {
					hand.add( s.deck().deal() );
				}
				else {
					break;
				}
			}
		}
		
		final ArrayList<int[]> pv = s.player_hand.values();
		assert( s.r == 0 );
		
		if( s.player_hand.Nhands == 1
				&& pv.get( 0 )[0] == SpBjHand.max_score
				&& s.player_hand.hands.get( 0 ).size() == 2 ) {
			// Natural Blackjack always wins and pays 3:2
			s.r += 3*s.player_hand.bets[0] / 2;
			return;
		}
		
		for( int i = 0; i < pv.size(); ++i ) {
			final int[] pvi = pv.get( i );
			// Player bust
			if( pvi[0] > SpBjHand.max_score ) {
				s.r += -s.player_hand.bets[i];
			}
			// Player has 21
			else if( pvi[0] == SpBjHand.max_score ) {
				// Player didn't double -> eligible for bonuses
				if( s.player_hand.bets[i] == SpBjHand.min_bet ) {
					final ArrayList<Card> h = s.player_hand.hands.get( i );
					if( h.size() == 5 ) { // 5-card 21 pays 3:2
						s.r += 3*s.player_hand.bets[i] / 2;
						continue;
					}
					else if( h.size() == 6 ) { // 6-card 21 pays 2:1
						s.r += 2*s.player_hand.bets[i];
						continue;
					}
					else if( h.size() >= 7 ) { // 7-or-more-card 21 pays 3:1
						s.r += 3*s.player_hand.bets[i];
						continue;
					}
					else if( h.size() == 3 ) {
						// Check for 678 / 777 bonus
						final ArrayList<Card> sorted = Fn.copy( h );
						Collections.sort( sorted, Card.TheAceHighRankComparator );
						final Card c1 = sorted.get( 1 );
						if( c1.rank == Rank.R_7 ) {
							final Card c0 = sorted.get( 0 );
							final Card c2 = sorted.get( 2 );
							if( (c0.rank == Rank.R_6 && c2.rank == Rank.R_8)
									|| (c0.rank == Rank.R_7 && c2.rank == Rank.R_7) ) {
								if( c0.suit == c1.suit && c1.suit == c2.suit ) {
									// Suited
									if( c0.suit == Suit.S_s ) {
										// Spades pay 3:1
										s.r += 3*s.player_hand.bets[i];
										continue;
									}
									else {
										// Non-spades pay 2:1
										s.r += 2*s.player_hand.bets[i];
										continue;
									}
								}
								else {
									// Not suited pays 3:2
									s.r += 3*s.player_hand.bets[i] / 2;
									continue;
								}
							}
						}
					}
				}
				
				// No bonuses, but player 21 always wins
				s.r += s.player_hand.bets[i];
			}
			// Dealer bust or player beats dealer
			else if( dv[0] > SpBjHand.max_score || pvi[0] > dv[0] ) {
				s.r += s.player_hand.bets[i];
			}
			// Dealer beats player
			else if( pvi[0] < dv[0] ) {
				s.r += -s.player_hand.bets[i];
			}
			else {
				assert( pvi[0] == dv[0] );
				// Ties are pushes
			}
		}
	}
	
	@Override
	public void undoAction( final SpBjState s )
	{
		assert( done );
		
		if( terminal ) {
			uncompleteDealerHand( s );
			terminal = false;
		}
		
		for( int i = cat.length - 1; i >= 0; --i ) {
			final SpBjHand h = s.player_hand;
			final ArrayList<Card> cards = h.hands.get( i );
			switch( cat[i] ) {
				case Hit: {
					final Card c = cards.remove( cards.size() - 1 );
					s.deck().undeal( c );
					h.passed[i] = false;
					break;
				}
				case Pass:
					h.passed[i] = false;
					break;
				case Split: {
					final int j = h.children[i];
					final ArrayList<Card> child = h.hands.get( j );
					assert( child.size() == 2 );
					assert( cards.size() == 2 );
					s.deck().undeal( child.remove( child.size() - 1 ) );
					s.deck().undeal( cards.remove( cards.size() - 1 ) );
					cards.add( child.remove( 0 ) );
					h.Nhands -= 1;
					h.children[i] = 0;
					h.bets[j] = 0;
					h.passed[j] = true;
					h.hands.remove( h.hands.size() - 1 );
					break;
				}
				case Double: {
					h.bets[i] /= 2;
					final Card c = cards.remove( cards.size() - 1 );
					s.deck().undeal( c );
					h.passed[i] = false;
					break;
				}
			}
		}
		
		done = false;
	}
	
	private void uncompleteDealerHand( final SpBjState s )
	{
		final ArrayList<Card> hand = s.dealerHand();
		while( hand.size() > 1 ) {
			s.deck().undeal( hand.remove( hand.size() - 1 ) );
		}
		s.r = 0;
	}

	@Override
	public boolean isDone()
	{
		return done;
	}
	
	@Override
	public String toString()
	{
		return Arrays.toString( cat );
	}
}
