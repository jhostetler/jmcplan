/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class YahtzeeState implements State
{
	private Hand hand_ = null;
	
	public final int[] scores = new int[YahtzeeScores.values().length];
	public final boolean[] filled = new boolean[YahtzeeScores.values().length];
	public int upper_total = 0;
	public int total = 0;
	public int yahtzee_bonus = 0;
	public int upper_bonus = 0;
	public int rerolls = 0;
	
	private final RandomGenerator rng_;
	
	public YahtzeeState( final RandomGenerator rng )
	{
		rng_ = rng;
	}
	
	public int score()
	{
		return total + yahtzee_bonus + upper_bonus;
	}
	
	public int[] roll( final int n )
	{
//		assert( n > 0 );
		assert( n <= Hand.Ndice );
		final int[] r = new int[Hand.Nfaces];
		for( int i = 0; i < n; ++i ) {
			final int d = rng_.nextInt( Hand.Nfaces );
			r[d] += 1;
		}
		return r;
	}
	
	public Hand hand()
	{
		return hand_;
	}
	
	public void setHand( final Hand h, final int rerolls )
	{
		hand_ = h;
		this.rerolls = rerolls;
	}
	
	public void addScore( final YahtzeeScores cat, final int score, final int yahtzee_bonus )
	{
		final int i = cat.ordinal();
		assert( !filled[i] );
		scores[i] += score;
		total += score;
		if( cat.isUpper() ) {
			upper_total += score;
			if( upper_total >= 63 ) {
				upper_bonus = 35;
			}
		}
		filled[i] = true;
		
		this.yahtzee_bonus += yahtzee_bonus;
	}
	
	public void subtractScore( final YahtzeeScores cat, final int score, final int yahtzee_bonus )
	{
		final int i = cat.ordinal();
		assert( filled[i] );
		scores[i] -= score;
		total -= score;
		if( cat.isUpper() ) {
			upper_total -= score;
			if( upper_total < 63 ) {
				upper_bonus = 0;
			}
		}
		filled[i] = false;
		
		this.yahtzee_bonus -= yahtzee_bonus;
	}
	
	public boolean yahtzeeBonusActive()
	{
		return scores[YahtzeeScores.Yahtzee.ordinal()] > 0;
	}
	
	@Override
	public boolean isTerminal()
	{
		return Fn.all( filled );
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		for( final YahtzeeScores category : YahtzeeScores.values() ) {
			sb.append( String.format( "%-20s%s %3d\n",
					   category, (filled[category.ordinal()] ? "x" : ":"), scores[category.ordinal()] ) );
		}
		sb.append( String.format( "Upper bonus:   %d\n", upper_bonus ) );
		sb.append( String.format( "Yahtzee bonus: %d\n", yahtzee_bonus ) );
		sb.append( String.format( "Total: ------> %d\n", score() ) );
		sb.append( "Hand: " ).append( Arrays.toString( hand().dice ) ).append( "\n" );
		return sb.toString();
	}
}
