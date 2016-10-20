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

import org.apache.commons.math3.random.RandomGenerator;


/**
 * Scores in the open box with the minimum max score.
 * 
 * @author jhostetler
 *
 */
public class ScoreMinMaxAction extends YahtzeeAction
{
	public YahtzeeScores category = null;
	
	private Hand old_hand_ = null;
	private int old_rerolls_ = 0;
	
	private int score_ = 0;
	private int yahtzee_bonus_ = 0;
	
	@Override
	public void undoAction( final YahtzeeState s )
	{
		assert( old_hand_ != null );
		s.setHand( old_hand_, old_rerolls_ );
		old_hand_ = null;
		old_rerolls_ = 0;
		s.subtractScore( category, score_, yahtzee_bonus_ );
		score_ = yahtzee_bonus_ = 0;
	}

	@Override
	public void doAction( final RandomGenerator rng, final YahtzeeState s )
	{
		assert( old_hand_ == null );
		
		final Hand h = s.hand();
		old_hand_ = h;
		old_rerolls_ = s.rerolls;
		
		int best_scalar = Integer.MAX_VALUE;
		for( final YahtzeeScores category : YahtzeeScores.values() ) {
			if( !s.filled[category.ordinal()] ) {
				final int v = category.maxScore();
				if( v < best_scalar ) {
					best_scalar = v;
					this.category = category;
				}
			}
		}
		final int[] value = h.value( s, category );
		score_ = value[0];
		yahtzee_bonus_ = value[1];
		assert( !s.filled[category.ordinal()] );
		s.addScore( category, score_, yahtzee_bonus_ );
		
		final int[] r = s.roll( rng, Hand.Ndice );
		s.setHand( new Hand( r ), Hand.Nrerolls );
	}

	@Override
	public boolean isDone()
	{
		return old_hand_ != null;
	}

	@Override
	public ScoreMinMaxAction create()
	{
		return new ScoreMinMaxAction();
	}
	
	@Override
	public String toString()
	{
		return "ScoreMinMax";
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof ScoreMinMaxAction) ) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode()
	{
		return 67;
	}
}