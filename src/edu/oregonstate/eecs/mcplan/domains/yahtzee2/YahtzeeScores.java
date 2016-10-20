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

/**
 * @author jhostetler
 *
 */
public enum YahtzeeScores implements YahtzeeScoreCategory
{
	// -----------------------------------------------------------------------
	// Upper section
	// -----------------------------------------------------------------------
	
	Ones {
		@Override
		public boolean isSatisfiedBy( final Hand h )
		{ return true; }
		
		@Override
		public int score( final Hand h )
		{ return 1 * h.dice[0]; }
		
		@Override
		public int maxScore()
		{ return 1 * Hand.Ndice; }
		
		@Override
		public boolean isUpper()
		{ return true; }
	},
	Twos {
		@Override
		public boolean isSatisfiedBy( final Hand h )
		{ return true; }
		
		@Override
		public int score( final Hand h )
		{ return 2 * h.dice[1]; }
		
		@Override
		public int maxScore()
		{ return 2 * Hand.Ndice; }
		
		@Override
		public boolean isUpper()
		{ return true; }
	},
	Threes {
		@Override
		public boolean isSatisfiedBy( final Hand h )
		{ return true; }
		
		@Override
		public int score( final Hand h )
		{ return 3 * h.dice[2]; }
		
		@Override
		public int maxScore()
		{ return 3 * Hand.Ndice; }
		
		@Override
		public boolean isUpper()
		{ return true; }
	},
	Fours {
		@Override
		public boolean isSatisfiedBy( final Hand h )
		{ return true; }
		
		@Override
		public int score( final Hand h )
		{ return 4 * h.dice[3]; }
		
		@Override
		public int maxScore()
		{ return 4 * Hand.Ndice; }
		
		@Override
		public boolean isUpper()
		{ return true; }
	},
	Fives {
		@Override
		public boolean isSatisfiedBy( final Hand h )
		{ return true; }
		
		@Override
		public int score( final Hand h )
		{ return 5 * h.dice[4]; }
		
		@Override
		public int maxScore()
		{ return 5 * Hand.Ndice; }
		
		@Override
		public boolean isUpper()
		{ return true; }
	},
	Sixes {
		@Override
		public boolean isSatisfiedBy( final Hand h )
		{ return true; }
		
		@Override
		public int score( final Hand h )
		{ return 6 * h.dice[5]; }
		
		@Override
		public int maxScore()
		{ return 6 * Hand.Ndice; }
		
		@Override
		public boolean isUpper()
		{ return true; }
	},
	
	// -----------------------------------------------------------------------
	// Lower section
	// -----------------------------------------------------------------------
	
	ThreeOfKind {
		@Override
		public boolean isSatisfiedBy( final Hand h )
		{ return h.nok_n >= 3; }
		
		@Override
		public int score( final Hand h )
		{ return h.sum; }
		
		@Override
		public int maxScore()
		{ return 6 * Hand.Ndice; }
		
		@Override
		public boolean isUpper()
		{ return false; }
	},
	FourOfKind {
		@Override
		public boolean isSatisfiedBy( final Hand h )
		{ return h.nok_n >= 4; }
		
		@Override
		public int score( final Hand h )
		{ return h.sum; }
		
		@Override
		public int maxScore()
		{ return 6 * Hand.Ndice; }
		
		@Override
		public boolean isUpper()
		{ return false; }
	},
	SmallStraight {
		@Override
		public boolean isSatisfiedBy( final Hand h )
		{ return h.straight() >= 4; }
		
		@Override
		public int score( final Hand h )
		{ return 30; }
		
		@Override
		public int maxScore()
		{ return 30; }
		
		@Override
		public boolean isUpper()
		{ return false; }
	},
	LargeStraight {
		@Override
		public boolean isSatisfiedBy( final Hand h )
		{ return h.straight() >= 5; }

		@Override
		public int score( final Hand h )
		{ return 40; }
		
		@Override
		public int maxScore()
		{ return 40; }
		
		@Override
		public boolean isUpper()
		{ return false; }
	},
	FullHouse {
		@Override
		public boolean isSatisfiedBy( final Hand h )
		{ return h.fh_pair > 0; }
		
		@Override
		public int score( final Hand h )
		{ return 25; }
		
		@Override
		public int maxScore()
		{ return 25; }
		
		@Override
		public boolean isUpper()
		{ return false; }
	},
	Yahtzee {
		@Override
		public boolean isSatisfiedBy( final Hand h )
		{ return h.nok_n >= 5; }
		
		@Override
		public int score( final Hand h )
		{ return 50; }
		
		@Override
		public int maxScore()
		{ return 50; }
		
		@Override
		public boolean isUpper()
		{ return false; }
	},
	Chance {
		@Override
		public boolean isSatisfiedBy( final Hand h )
		{ return true; }
		
		@Override
		public int score( final Hand h )
		{ return h.sum; }
		
		@Override
		public int maxScore()
		{ return 6 * Hand.Ndice; }
		
		@Override
		public boolean isUpper()
		{ return false; }
	};
	
	// -----------------------------------------------------------------------
	
	public static YahtzeeScores upper( final int i )
	{
		switch( i ) {
		case 1: return Ones;
		case 2: return Twos;
		case 3: return Threes;
		case 4: return Fours;
		case 5: return Fives;
		case 6: return Sixes;
		default:
			throw new AssertionError();
		}
	}
}
