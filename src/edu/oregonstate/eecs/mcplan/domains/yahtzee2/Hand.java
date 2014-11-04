/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import java.util.Arrays;

/**
 * FIXME: The immutable data for the straights has been removed because it was
 * subtly incorrect. It can be made correct again, but it's probably not the
 * most critical thing in terms of performance. The function implementations are correct.
 */
public class Hand
{
	// TODO: This isn't the best place to put 'Nrerolls'
	public static final int Nrerolls = 2;
	public static final int Ndice = 5;
	public static final int Nfaces = 6;
	
	/**
	 * Size 'Nfaces'. Histogram of dice values.
	 */
	public final int[] dice;
	
	public final int sum;
	public final int nok_n;
	public final int nok_i;
//	public final int straight;
//	public final int straight_len;
	public final int fh_pair;
	
	public Hand( final int[] dice )
	{
		assert( dice.length == Nfaces );
		this.dice = dice;
		
		int sum = 0;
		int nok_n = 0;
		int nok_i = 0;
//		int straight = 0;
//		int straight_len = 0;
		int fh_pair = 0;
		for( int i = 0; i < Nfaces; ++i ) {
			final int n = dice[i];
			final int value = i + 1;
			sum += value * n;
			if( n > 0 ) {
//				straight_len += 1;
				if( n == 2 ) {
					fh_pair = value;
				}
				else if( n >= 3 ) {
					nok_n = n;
					nok_i = value;
				}
			}
//			else if( straight_len >= 4 ) { // It's at least a small straight
//				straight = value - straight_len;
//			}
//			else {
//				straight_len = 0;
//			}
		}
		
		this.sum = sum;
		this.nok_n = nok_n;
		this.nok_i = nok_i;
//		this.straight = straight;
//		this.straight_len = straight_len;
		this.fh_pair = fh_pair;
	}
	
	public int[] value( final YahtzeeState s, final YahtzeeScores category )
	{
		final int[] v = new int[2];
		if( YahtzeeScores.Yahtzee.isSatisfiedBy( this ) ) {
			// Note: The rules are ambiguous about Yahtzee scoring wrt.
			// Jokers, namely whether you can score a Yahtzee in e.g.
			// ThreeOfKind if you don't have the corresponding Upper section
			// yet. I've decided to allow this, for the sake of consistency.
			// You're still not allowed to score Straight/FullHouse unless
			// you have the upper section.
			if( s.yahtzeeBonusActive() ) {
				v[1] = 100;
			}
			if( category == YahtzeeScores.Yahtzee ) { // Normal Yahtzee
				v[0] = category.score( this );
			}
			else if( category.isSatisfiedBy( this ) ) {
				v[0] = category.score( this );
			}
			else if( s.filled[YahtzeeScores.Yahtzee.ordinal()]
					 && s.filled[YahtzeeScores.upper( this.nok_i ).ordinal()] ) { // Scoring Yahtzee as Joker
				v[0] = category.score( this );
			}
			else {
				v[0] = 0;
			}
		}
		else if( category.isSatisfiedBy( this ) ) {
			v[0] = category.score( this );
		}
		else {
			v[0] = 0;
		}
		return v;
	}
	
	public int sum()
	{
		int s = 0;
		for( int i = 0; i < Nfaces; ++i ) {
			s += dice[i] * (i+1);
		}
		return s;
	}
	
	public int nOfKind()
	{
		for( int i = 0; i < Nfaces; ++i ) {
			if( dice[i] >= 3 ) {
				return dice[i];
			}
		}
		return 0;
	}
	
	public int straight()
	{
		int s = 0;
		for( int i = 0; i < Nfaces; ++i ) {
			if( dice[i] > 0 ) {
				s += 1;
			}
			else if( s >= 4 ) { // It's at least a small straight
				return s;
			}
			else {
				s = 0;
			}
		}
		return (s >= 4 ? s : 0);
	}
	
	public int[] fullHouse()
	{
		final int[] h = new int[2];
		for( int i = 0; i < Nfaces; ++i ) {
			if( dice[i] == 3 ) {
				h[0] = i + 1;
			}
			else if( dice[i] == 2 ) {
				h[1] = i + 1;
			}
		}
		if( h[0] > 0 && h[1] > 0 ) {
			return h;
		}
		else {
			return null;
		}
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		final Hand that = (Hand) obj;
		return Arrays.equals( this.dice, that.dice );
	}
	
	@Override
	public int hashCode()
	{
		return Arrays.hashCode( dice );
	}
	
	@Override
	public String toString()
	{
		return Arrays.toString( dice );
	}
}
