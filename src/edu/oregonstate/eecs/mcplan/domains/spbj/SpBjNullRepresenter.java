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
import java.util.Collections;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ArrayFactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.domains.cards.Card;
import edu.oregonstate.eecs.mcplan.domains.cards.Rank;
import edu.oregonstate.eecs.mcplan.domains.cards.Suit;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class SpBjNullRepresenter implements FactoredRepresenter<SpBjState, FactoredRepresentation<SpBjState>>
{
	private static final ArrayList<Attribute> attributes;
	private static final int Nfeatures;
	private static final int Nplayer_hand_features = 13;
	
	// When you fix this, since you have to re-run anyway, consider changing
	// the rules to the "official rules":
	// http://www.wsgc.wa.gov/activities/game-rules.aspx
	static {
		attributes = new ArrayList<Attribute>();
		// Player attributes
		for( int i = 0; i < SpBjHand.max_hands; ++i ) {
			attributes.add( new Attribute( "bet" + i ) );
			attributes.add( new Attribute( "value" + i ) );
			attributes.add( new Attribute( "cards" + i ) );
			attributes.add( new Attribute( "soft_aces" + i ) );
			attributes.add( new Attribute( "passed" + i ) );
			attributes.add( new Attribute( "can_split" + i ) );
			attributes.add( new Attribute( "can_double" + i ) );
			
			attributes.add( new Attribute( "off_2of3" ) );
			attributes.add( new Attribute( "suit_2of3" ) );
			attributes.add( new Attribute( "spade_2of3" ) );
			attributes.add( new Attribute( "off_3of3" ) );
			attributes.add( new Attribute( "suit_3of3" ) );
			attributes.add( new Attribute( "spade_3of3" ) );
			
//			attributes.add( new Attribute( "off_678" ) );
//			attributes.add( new Attribute( "suit_678" ) );
//			attributes.add( new Attribute( "spade_678" ) );
//			attributes.add( new Attribute( "off_777" ) );
//			attributes.add( new Attribute( "suit_777" ) );
//			attributes.add( new Attribute( "spade_777" ) );
		}
		assert( attributes.size() == SpBjHand.max_hands * Nplayer_hand_features );
		// Dealer attributes
		attributes.add( new Attribute( "dealer_showing" ) );
		attributes.add( new Attribute( "dealer_value" ) );
		Nfeatures = attributes.size();
	}
	
	@Override
	public FactoredRepresenter<SpBjState, FactoredRepresentation<SpBjState>> create()
	{
		return new SpBjNullRepresenter();
	}
	
	// Note: Code relies on the ordering of these values.
	private static final int BONUS_NONE = -1;
	
	private static final int BONUS_OFF_2of3 = 0;
	private static final int BONUS_SUIT_2of3 = 1;
	private static final int BONUS_SPADE_2of3 = 2;
	private static final int BONUS_OFF_3of3 = 3;
	private static final int BONUS_SUIT_3of3 = 4;
	private static final int BONUS_SPADE_3of3 = 5;
	
	private int bonus( final ArrayList<Card> h )
	{
		if( h.size() == 2 ) {
//			final ArrayList<Card> sorted = Fn.copy( h );
//			Collections.sort( sorted, Card.TheAceHighRankComparator );
//			final Card c0 = sorted.get( 0 );
//			final Card c1 = sorted.get( 1 );
			
			final Card c0 = h.get( 0 );
			final Card c1 = h.get( 1 );
			
			final boolean bonus_2of3;
			if( c0.rank == Rank.R_6 ) {
				bonus_2of3 = (c1.rank == Rank.R_7 || c1.rank == Rank.R_8);
			}
			else if( c0.rank == Rank.R_7 ) {
				bonus_2of3 = (c1.rank == Rank.R_6 || c1.rank == Rank.R_7 || c1.rank == Rank.R_8);
			}
			else if( c0.rank == Rank.R_8 ) {
				bonus_2of3 = (c1.rank == Rank.R_6 || c1.rank == Rank.R_7);
			}
			else {
				bonus_2of3 = false;
			}
			
			if( !bonus_2of3 ) {
				return BONUS_NONE;
			}
			else if( c0.suit != c1.suit ) {
				return BONUS_OFF_2of3;
			}
			else {
				if( c0.suit == Suit.S_s ) {
					return BONUS_SPADE_2of3;
				}
				else {
					return BONUS_SUIT_2of3;
				}
			}
		}
		else if( h.size() == 3 ) {
			final ArrayList<Card> sorted = Fn.copy( h );
			Collections.sort( sorted, Card.TheAceHighRankComparator );
			
			final Card c1 = sorted.get( 1 );
			if( c1.rank == Rank.R_7 ) {
				final Card c0 = sorted.get( 0 );
				final Card c2 = sorted.get( 2 );
				final boolean bonus;
				if( c0.rank == Rank.R_6 && c2.rank == Rank.R_8 ) {
					bonus = true;
				}
				else if( c0.rank == Rank.R_7 && c2.rank == Rank.R_7 ) {
					bonus = true;
				}
				else {
					bonus = false;
				}
				
				if( !bonus ) {
					return BONUS_NONE;
				}
				
				if( c0.suit == c1.suit && c1.suit == c2.suit ) {
					// Suited
					if( c0.suit == Suit.S_s ) {
						return BONUS_SPADE_3of3;
					}
					else {
						return BONUS_SUIT_3of3;
					}
				}
				else {
					return BONUS_OFF_3of3;
				}
			}
		}
		
		return BONUS_NONE;
	}
	
//	private static final int BONUS_OFF_678 = 0;
//	private static final int BONUS_SUIT_678 = 1;
//	private static final int BONUS_SPADE_678 = 2;
//	private static final int BONUS_OFF_777 = 3;
//	private static final int BONUS_SUIT_777 = 4;
//	private static final int BONUS_SPADE_777 = 5;
//
//	private int bonus( final ArrayList<Card> h )
//	{
//		if( h.size() != 3 ) {
//			return BONUS_NONE;
//		}
//
//		// Check for 678 / 777 bonus
//		final ArrayList<Card> sorted = Fn.copy( h );
//		Collections.sort( sorted, Card.TheAceHighRankComparator );
//		final Card c1 = sorted.get( 1 );
//		if( c1.rank == Rank.R_7 ) {
//			final Card c0 = sorted.get( 0 );
//			final Card c2 = sorted.get( 2 );
//			final int offset;
//			if( c0.rank == Rank.R_6 && c2.rank == Rank.R_8 ) {
//				offset = BONUS_OFF_678;
//			}
//			else if( c0.rank == Rank.R_7 && c2.rank == Rank.R_7 ) {
//				offset = BONUS_OFF_777;
//			}
//			else {
//				return BONUS_NONE;
//			}
//
//			if( c0.suit == c1.suit && c1.suit == c2.suit ) {
//				// Suited
//				if( c0.suit == Suit.S_s ) {
//					return offset + 2;
//				}
//				else {
//					return offset + 1;
//				}
//			}
//			else {
//				return offset;
//			}
//		}
//
//		return BONUS_NONE;
//	}
	
	

	@Override
	public FactoredRepresentation<SpBjState> encode( final SpBjState s )
	{
		final float[] phi = new float[Nfeatures];
		int idx = 0;
		// Player attributes
		final SpBjHand h = s.player_hand;
		final boolean[] busted = new boolean[h.hands.size()];
		final int busted_score = 22;
		for( int i = 0; i < SpBjHand.max_hands; ++i ) {
			// No i'th hand
			if( i >= h.hands.size() ) {
				for( int j = 0; j < Nplayer_hand_features; ++j ) {
					phi[idx++] = 0;
				}
				continue;
			}
			
			final ArrayList<Card> hi = h.hands.get( i );
			final int[] v = SpBjHand.handValue( h.hands.get( i ) );
//			attributes.add( new Attribute( "bet" + i ) );
			phi[idx++] = h.bets[i];
//			attributes.add( new Attribute( "value" + i ) );
			
			final int capped_value = Math.min( v[0], busted_score );
			if( capped_value == busted_score ) {
				busted[i] = true;
			}
			phi[idx++] = capped_value;
//			attributes.add( new Attribute( "cards" + i ) );
			phi[idx++] = h.hands.get( i ).size();
//			attributes.add( new Attribute( "soft_aces" + i ) );
			phi[idx++] = v[1];
			if( !h.passed[i] ) {
	//			attributes.add( new Attribute( "passed" + i ) );
				phi[idx++] = 0;
	//			attributes.add( new Attribute( "can_split" + i ) );
				phi[idx++] = (h.canSplit( i ) ? 1 : 0);
	//			attributes.add( new Attribute( "can_double" + i ) );
				phi[idx++] = (h.canDouble( i ) ? 1 : 0);
			}
			else {
				phi[idx++] = 1;
				phi[idx++] = 0;
				phi[idx++] = 0;
			}
			
//			attributes.add( new Attribute( "off_678" ) );
//			attributes.add( new Attribute( "suit_678" ) );
//			attributes.add( new Attribute( "spade_678" ) );
//			attributes.add( new Attribute( "off_777" ) );
//			attributes.add( new Attribute( "suit_777" ) );
//			attributes.add( new Attribute( "spade_777" ) );
			final int bonus = bonus( hi );
			for( int j = 0; j < 6; ++j ) {
				// Note: -1 indicates "no bonus"
				phi[idx++] = (j == bonus ? 1 : 0);
			}
		}
		
		// Dealer attributes -- leave them at 0 if player busted
		if( !Fn.all( busted ) ) {
	//		attributes.add( new Attribute( "dealer_showing" ) );
			phi[idx++] = s.dealerUpcard().BlackjackValue();
	//		attributes.add( new Attribute( "dealer_value" ) );
			if( s.isTerminal() ) {
				phi[idx++] = SpBjHand.handValue( s.dealerHand() )[0];
			}
			else {
				phi[idx++] = 0;
			}
		}
		
		// Assertion doesn't hold if we skip the dealer due to busting
//		assert( idx == phi.length );
		return new ArrayFactoredRepresentation<SpBjState>( phi );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}

}
