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
 * @deprecated
 * @author jhostetler
 */
@Deprecated
public abstract class YahtzeeHand
{
	public static final int Ndice = 5;
	public static final int Nfaces = 6;
	
	public static YahtzeeHand of( final int[] dice )
	{
		return new FullHouse( dice, 1, 2 );
	}
	
	// -----------------------------------------------------------------------
	
	public final int[] dice;
	public final int sum;
	
	protected YahtzeeHand( final int[] dice )
	{
		this.dice = dice;
		this.sum = sum( dice );
	}
	
	public int threeOfKindScore( final YahtzeeState s ) { return 0; }
	public int fourOfKindScore( final YahtzeeState s ) { return 0; }
	public int smallStraightScore( final YahtzeeState s ) { return 0; }
	public int largeStraightScore( final YahtzeeState s ) { return 0; }
	public int fullHouseScore( final YahtzeeState s ) { return 0; }
	public int yahtzeeScore( final YahtzeeState s ) { return 0; }
	
	private static int sum( final int[] dice )
	{
		int s = 0;
		for( int i = 0; i < Ndice; ++i ) {
			s += dice[i] * (i+1);
		}
		return s;
	}
	
	// -----------------------------------------------------------------------
	
	public static final class ThreeOfKind extends YahtzeeHand
	{
		public final int i;
		public ThreeOfKind( final int[] dice, final int i )
		{ super( dice ); this.i = i; }
		
		@Override
		public int threeOfKindScore( final YahtzeeState s )
		{ return sum; }
	}
	
	public static final class FourOfKind extends YahtzeeHand
	{
		public final int i;
		public FourOfKind( final int[] dice, final int i )
		{ super( dice ); this.i = i; }
		
		@Override
		public int threeOfKindScore( final YahtzeeState s )
		{ return sum; }
		
		@Override
		public int fourOfKindScore( final YahtzeeState s )
		{ return sum; }
	}
	
	private static final class FullHouse extends YahtzeeHand
	{
		public final int i;
		public final int j;
		public FullHouse( final int[] dice, final int i, final int j )
		{ super( dice ); this.i = i; this.j = j; }
		
		@Override
		public int threeOfKindScore( final YahtzeeState s )
		{ return sum; }
		
		@Override
		public int fullHouseScore( final YahtzeeState s )
		{ return 25; }
	}
	
	private static final class SmallStraight extends YahtzeeHand
	{
		public final int i;
		public SmallStraight( final int[] dice, final int i )
		{ super( dice ); this.i = i; }
		
		@Override
		public int smallStraightScore( final YahtzeeState s )
		{ return 30; }
	}
	
	private static final class LargeStraight extends YahtzeeHand
	{
		public final int i;
		public LargeStraight( final int[] dice, final int i )
		{ super( dice ); this.i = i; }
		
		@Override
		public int smallStraightScore( final YahtzeeState s )
		{ return 30; }
		
		@Override
		public int largeStraightScore( final YahtzeeState s )
		{ return 40; }
	}
	
	public static final class Yahtzee extends YahtzeeHand
	{
		public final int i;
		public Yahtzee( final int[] dice, final int i )
		{ super( dice ); this.i = i; }
		
		@Override
		public int threeOfKindScore( final YahtzeeState s )
		{ return sum; }
		
		@Override
		public int fourOfKindScore( final YahtzeeState s )
		{ return sum; }
		
		
	}
}
