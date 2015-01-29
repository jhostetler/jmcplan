package edu.oregonstate.eecs.mcplan.domains.spbj;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.domains.cards.Card;
import edu.oregonstate.eecs.mcplan.domains.cards.Rank;
import edu.oregonstate.eecs.mcplan.util.Fn;

public class SpBjHand
{
	/** Maximum un-busted score. */
	public static final int max_score = 21;
	/** Minimum busted score. */
	public static final int busted_score = max_score + 1;
	/** Maximum value that dealer hits. */
	public static final int dealer_threshold = max_score - 5;
	
	/** Number of distinct values dealer can be showing. */
	public static final int dealer_showing_count = 10;
	/** Smallest value dealer can be showing. */
	public static final int dealer_showing_min = 2;
	
	/** Smallest possible value of a hard hand. */
	public static final int hard_hand_min = 4;
	/** Number of distinct hard hand values. */
	public static final int hard_hand_count = max_score - hard_hand_min + 1;
	
	/** Smallest possible soft hand value. */
	public static final int soft_hand_min = 12;
	/** Number of distinct soft hand values. */
	public static final int soft_hand_count = max_score - soft_hand_min + 1;
	
	/** Maximum number of simultaneous hands for player. */
	public static final int max_hands = 4;
	/** Maximum number of doublings for player. */
	public static final int max_doubles = 3;
	
	/**
	 * The minimum bet (starting bet). We set this to 2 so that 3:2 odds
	 * results in an integer number of chips.
	 */
	public static final int min_bet = 2;
	/** The maximum bet (after the max number of doubles). */
	public static final int max_bet = (min_bet << max_doubles);
	
	public static int[] handValue( final Iterable<Card> hand )
	{
		int total = 0;
		int high_aces = 0;
		for( final Card c : hand ) {
			total += c.BlackjackValue();
			if( c.rank == Rank.R_A ) {
				high_aces += 1;
			}
			while( total > max_score && high_aces > 0 ) {
				total -= 10;
				high_aces -= 1;
			}
		}
		return new int[] { total, high_aces };
	}
	
	// -----------------------------------------------------------------------
	
	public final ArrayList<ArrayList<Card>> hands = new ArrayList<ArrayList<Card>>();
	public final int[] bets = new int[max_hands];
	public final boolean[] passed = new boolean[max_hands];
	public int Nhands = 0;
	/**
	 * Stores the index of the hand split off from this hand.
	 */
	public final int[] children = new int[max_hands];
	
	public SpBjHand()
	{
		for( int i = 0; i < max_hands; ++i ) {
			passed[i] = true;
		}
	}
	
	public SpBjHand copy()
	{
		final SpBjHand copy = new SpBjHand();
		for( final ArrayList<Card> h : hands ) {
			final ArrayList<Card> hcopy = new ArrayList<Card>();
			hcopy.addAll( h );
			copy.hands.add( hcopy );
		}
		Fn.memcpy( copy.bets, bets );
		Fn.memcpy( copy.passed, passed );
		copy.Nhands = Nhands;
		Fn.memcpy( copy.children, children );
		
		return copy;
	}
	
	public boolean isPassed( final int i )
	{
		return passed[i];
	}
	
	public boolean canSplit( final int i )
	{
		if( Nhands >= max_hands ) {
			return false;
		}
		final ArrayList<Card> cards = hands.get( i );
		return cards.size() == 2
			   && cards.get( 0 ).rank == cards.get( 1 ).rank;
	}
	
	public boolean canDouble( final int i )
	{
		return bets[i] < SpBjHand.max_bet;
	}
	
	public ArrayList<int[]> values()
	{
		final ArrayList<int[]> v = new ArrayList<int[]>();
		for( int i = 0; i < Nhands; ++i ) {
			final ArrayList<Card> hand = hands.get( i );
			v.add( handValue( hand ) );
		}
		return v;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "{" );
		for( int i = 0; i < Nhands; ++i ) {
			if( i > 0 ) {
				sb.append( "; " );
			}
			sb.append( bets[i] ).append( "$" );
			sb.append( hands.get( i ) );
			if( passed[i] ) {
				sb.append( ":passed" );
			}
		}
		sb.append( "}" );
		return sb.toString();
	}
}
