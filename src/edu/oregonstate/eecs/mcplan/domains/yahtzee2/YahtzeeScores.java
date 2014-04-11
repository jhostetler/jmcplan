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
		{ return h.straight_len >= 4; }
		
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
		{ return h.straight_len >= 5; }

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
