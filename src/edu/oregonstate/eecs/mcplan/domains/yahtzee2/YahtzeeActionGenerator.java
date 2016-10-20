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

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class YahtzeeActionGenerator extends ActionGenerator<YahtzeeState, YahtzeeAction>
{
	private YahtzeeState s_ = null;
	private long t_ = 0L;
	
//	private final ArrayList<JointAction<YahtzeeAction>> actions_
//		= new ArrayList<JointAction<YahtzeeAction>>();
//	private final Iterator<JointAction<YahtzeeAction>> itr_ = null;
	
	private boolean rerolls_ = false;
	private Fn.MultisetPowerSetGenerator power_set_ = null;
	private int[] reference_hand_ = null;
	private int cat_idx_ = 0;
	private boolean[] filled_ = null;
	private int unfilled_ = 0;
	
	@Override
	public YahtzeeActionGenerator create()
	{
		return new YahtzeeActionGenerator();
	}

	@Override
	public void setState( final YahtzeeState s, final long t )
	{
		s_ = s;
		t_ = t;
		
		rerolls_ = s.rerolls > 0;
		final Hand h = s.hand();
		if( rerolls_ ) {
			reference_hand_ = Arrays.copyOf( h.dice, h.dice.length );
			power_set_ = new Fn.MultisetPowerSetGenerator( Arrays.copyOf( h.dice, h.dice.length ) );
		}
		cat_idx_ = 0;
		filled_ = Arrays.copyOf( s.filled, s.filled.length );
		unfilled_ = filled_.length - Fn.sum( filled_ );
		
//		actions_.clear();
//
//		// Convert hand to list of face *indices*
//		final int[] faces = new int[Hand.Ndice];
//		final Hand h = s.hand();
//		int idx = 0;
//		for( int i = 0; i < Hand.Nfaces; ++i ) {
//			for( int j = 0; j < h.dice[i]; ++j ) {
//				faces[idx++] = i;
//			}
//		}
//
//		if( s.rerolls > 0 ) {
//			// Add KeepAction for each subset of dice, *except* the entire set
//			for( final int[] subset : Fn.in( new Fn.MultisetPowerSetGenerator( h.dice ) ) ) {
//				actions_.add( new JointAction<YahtzeeAction>( new KeepAction( subset ) ) );
//			}
//			actions_.remove( actions_.size() - 1 ); // Drop the "entire set" subset
//		}
//
//		// Add ScoreAction for each open category
//		for( final YahtzeeScores category : YahtzeeScores.values() ) {
//			if( !s.filled[category.ordinal()] ) {
//				actions_.add( new JointAction<YahtzeeAction>( new ScoreAction( category ) ) );
//			}
//		}
//
//		itr_ = actions_.iterator();
	}
	
//	@Override
//	public void repeat()
//	{
//		itr_ = actions_.iterator();
//	}

	@Override
	public int size()
	{
		final int Nrerolls;
		if( rerolls_ ) {
			int r = 1;
			for( int i = 0; i < Hand.Nfaces; ++i ) {
				r *= (s_.hand().dice[i] + 1);
			}
			Nrerolls = r - 1; // -1 because "reroll none" is not allowed
		}
		else {
			Nrerolls = 0;
		}
		
		// Note: Old, wrong code:
//		final int Nrerolls = (rerolls_ ? 32 - 1 : 0); // 2^5, -1 not to count "reroll none"
		
		final int Nscore = unfilled_;
		return Nrerolls + Nscore;
	}

	@Override
	public boolean hasNext()
	{
		return rerolls_
			   || (cat_idx_ < YahtzeeScores.values().length && unfilled_ > 0);
	}

	@Override
	public YahtzeeAction next()
	{
		if( rerolls_ ) {
			while( power_set_.hasNext() ) {
				final int[] set = power_set_.next();
				if( !Arrays.equals( set, reference_hand_ ) ) {
					// Prohibit the "re-roll none" action
					return new KeepAction( set );
				}
			}
			rerolls_ = false;
		}
		
		while( cat_idx_ < YahtzeeScores.values().length ) {
			final YahtzeeScores category = YahtzeeScores.values()[cat_idx_++];
			if( !filled_[category.ordinal()] ) {
				filled_[category.ordinal()] = true;
				unfilled_ -= 1;
				return new ScoreAction( category );
			}
		}
		
		throw new AssertionError( "hasNext() == false" );
	}

}
