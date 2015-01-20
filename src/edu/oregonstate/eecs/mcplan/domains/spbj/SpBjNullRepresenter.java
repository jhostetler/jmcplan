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
			attributes.add( new Attribute( "off_678" ) );
			attributes.add( new Attribute( "suit_678" ) );
			attributes.add( new Attribute( "spade_678" ) );
			attributes.add( new Attribute( "off_777" ) );
			attributes.add( new Attribute( "suit_777" ) );
			attributes.add( new Attribute( "spade_777" ) );
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
	private static final int BONUS_OFF_678 = 0;
	private static final int BONUS_SUIT_678 = 1;
	private static final int BONUS_SPADE_678 = 2;
	private static final int BONUS_OFF_777 = 3;
	private static final int BONUS_SUIT_777 = 4;
	private static final int BONUS_SPADE_777 = 5;
	
	private int bonus( final ArrayList<Card> h )
	{
		if( h.size() != 3 ) {
			return BONUS_NONE;
		}
		
		// Check for 678 / 777 bonus
		final ArrayList<Card> sorted = Fn.copy( h );
		Collections.sort( sorted, Card.TheAceHighRankComparator );
		final Card c1 = sorted.get( 1 );
		if( c1.rank == Rank.R_7 ) {
			final Card c0 = sorted.get( 0 );
			final Card c2 = sorted.get( 2 );
			final int offset;
			if( c0.rank == Rank.R_6 && c2.rank == Rank.R_8 ) {
				offset = BONUS_OFF_678;
			}
			else if( c0.rank == Rank.R_7 && c2.rank == Rank.R_7 ) {
				offset = BONUS_OFF_777;
			}
			else {
				return BONUS_NONE;
			}
			
			if( c0.suit == c1.suit && c1.suit == c2.suit ) {
				// Suited
				if( c0.suit == Suit.S_s ) {
					return offset + 2;
				}
				else {
					return offset + 1;
				}
			}
			else {
				return offset;
			}
		}
		
		return BONUS_NONE;
	}

	@Override
	public FactoredRepresentation<SpBjState> encode( final SpBjState s )
	{
		final double[] phi = new double[Nfeatures];
		int idx = 0;
		// Player attributes
		final SpBjHand h = s.player_hand;
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
			phi[idx++] = v[0];
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
		
		// Dealer attributes
//		attributes.add( new Attribute( "dealer_showing" ) );
		phi[idx++] = s.dealerUpcard().BlackjackValue();
//		attributes.add( new Attribute( "dealer_value" ) );
		if( s.isTerminal() ) {
			phi[idx++] = SpBjHand.handValue( s.dealerHand() )[0];
		}
		else {
			phi[idx++] = 0;
		}
		
		assert( idx == phi.length );
		return new ArrayFactoredRepresentation<SpBjState>( phi );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}

}
