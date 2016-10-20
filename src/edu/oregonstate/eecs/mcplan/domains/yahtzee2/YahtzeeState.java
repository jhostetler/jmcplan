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
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import java.util.Arrays;

import org.apache.commons.lang3.builder.HashCodeBuilder;
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
	
	public YahtzeeState( final RandomGenerator rng )
	{
		// We roll an initial hand, so that rolling dice can be the last step
		// of KeepAction and ScoreAction, and thus we don't need a 'RollDice'
		// action, which would be the only action available whenever it is
		// legal.
		setHand( new Hand( roll( rng, Hand.Ndice ) ), Hand.Nrerolls );
	}
	
	public YahtzeeState( final YahtzeeState that )
	{
		hand_ = new Hand( that.hand().dice );
		
		Fn.memcpy( this.scores, that.scores );
		Fn.memcpy( this.filled, that.filled );
		this.upper_total = that.upper_total;
		this.total = that.total;
		this.yahtzee_bonus = that.yahtzee_bonus;
		this.upper_bonus = that.upper_bonus;
		this.rerolls = that.rerolls;
	}
	
	public int score()
	{
		return total + yahtzee_bonus + upper_bonus;
	}
	
	public int[] roll( final RandomGenerator rng, final int n )
	{
//		assert( n > 0 );
		assert( n <= Hand.Ndice );
		final int[] r = new int[Hand.Nfaces];
		for( int i = 0; i < n; ++i ) {
			final int d = rng.nextInt( Hand.Nfaces );
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
	public boolean equals( final Object obj )
	{
		final YahtzeeState that = (YahtzeeState) obj;
		
		return ( this.rerolls == that.rerolls
				 && this.total == that.total
				 && this.upper_total == that.upper_total
				 && Arrays.equals( this.filled, that.filled )
				 && Arrays.equals( this.scores, that.scores )
				 && hand_.equals( that.hand_ )
				 && this.yahtzee_bonus == that.yahtzee_bonus
				 && this.upper_bonus == that.upper_bonus );
	}
	
	@Override
	public int hashCode()
	{
		final HashCodeBuilder hb = new HashCodeBuilder( 3, 7 );
		hb.append( scores );
		hb.append( filled );
		hb.append( hand_ );
		hb.append( upper_total );
		hb.append( total );
		hb.append( yahtzee_bonus );
		hb.append( upper_bonus );
		hb.append( rerolls );
		
		return hb.toHashCode();
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

	@Override
	public void close()
	{ }
}
