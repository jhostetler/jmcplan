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

import edu.oregonstate.eecs.mcplan.util.Fn;


/**
 * @author jhostetler
 *
 */
public class CategoryAction extends YahtzeeAction
{
	public final YahtzeeScores category;
	
	private YahtzeeAction impl_ = null;
	
	public CategoryAction( final YahtzeeScores category )
	{
		this.category = category;
	}
	
	@Override
	public void undoAction( final YahtzeeState s )
	{
		impl_.undoAction( s );
		impl_ = null;
	}

	@Override
	public void doAction( final RandomGenerator rng, final YahtzeeState s )
	{
		if( s.rerolls > 0 ) { //&& !category.isSatisfiedBy( s.hand() ) ) {
			impl_ = improveCategory( s );
		}
		else {
			impl_ = new ScoreAction( category );
		}
		impl_.doAction( s );
	}

	private YahtzeeAction improveCategory( final YahtzeeState s )
	{
		final Hand h = s.hand();
		switch( category ) {
		case Ones:
			return new KeepAction( new int[] { h.dice[0], 0, 0, 0, 0, 0 } );
		case Twos:
			return new KeepAction( new int[] { 0, h.dice[1], 0, 0, 0, 0 } );
		case Threes:
			return new KeepAction( new int[] { 0, 0, h.dice[2], 0, 0, 0 } );
		case Fours:
			return new KeepAction( new int[] { 0, 0, 0, h.dice[3], 0, 0 } );
		case Fives:
			return new KeepAction( new int[] { 0, 0, 0, 0, h.dice[4], 0 } );
		case Sixes:
			return new KeepAction( new int[] { 0, 0, 0, 0, 0, h.dice[5] } );
		case ThreeOfKind: {
			final int i = Fn.argmax( Fn.reversed( h.dice ) );
			
			// Keep up to three of the highest-value dice we have the most of
			final int[] keepers = new int[Hand.Nfaces];
			keepers[i] = Math.min( h.dice[i], 3 );
			
			// Improve the dice not involved in the 3-of-kind
			if( s.rerolls >= 1 ) {
				keepers[5] = h.dice[5];
				keepers[4] = h.dice[4];
			}
			if( s.rerolls == 1 ) {
				keepers[3] = h.dice[3];
			}
			
			return new KeepAction( keepers );
		}
		case FourOfKind: { // Same as ThreeOfKind, except 4
			final int i = Fn.argmax( Fn.reversed( h.dice ) );
			
			// Keep up to three of the highest-value dice we have the most of
			final int[] keepers = new int[Hand.Nfaces];
			keepers[i] = Math.min( h.dice[i], 4 );
			
			// Improve the dice not involved in the 4-of-kind
			if( s.rerolls >= 1 ) {
				keepers[5] = h.dice[5];
				keepers[4] = h.dice[4];
			}
			if( s.rerolls == 1 ) {
				keepers[3] = h.dice[3];
			}
			
			return new KeepAction( keepers );
		}
		case Yahtzee: {
			final int i = Fn.argmax( Fn.reversed( h.dice ) );
			final int[] keepers = new int[Hand.Nfaces];
			keepers[i] = h.dice[i];
			return new KeepAction( keepers );
		}
		case SmallStraight:
		case LargeStraight:
			return new KeepStraightAction();
		case FullHouse:
			final int[] keepers = new int[Hand.Nfaces];
			boolean three = false;
			for( int i = Hand.Nfaces - 1; i >= 0; --i ) {
				if( h.dice[i] >= 3 ) {
					keepers[i] = 3;
					three = true;
					break;
				}
			}
			for( int i = Hand.Nfaces - 1; i >= 0; --i ) {
				if( h.dice[i] >= 2 ) {
					keepers[i] = 2;
					if( !three ) {
						three = true;
					}
					else {
						break;
					}
				}
			}
			return new KeepAction( keepers );
		case Chance:
			if( s.rerolls == 2 ) {
				return new KeepAction( new int[] { 0, 0, 0, 0, h.dice[4], h.dice[5] } );
			}
			else { // if( s.rerolls == 1 ) {
				return new KeepAction( new int[] { 0, 0, 0, h.dice[3], h.dice[4], h.dice[5] } );
			}
		default:
			throw new AssertionError( "unreachable" );
		}
	}

	@Override
	public boolean isDone()
	{
		return impl_ != null;
	}

	@Override
	public YahtzeeAction create()
	{
		return new CategoryAction( category );
	}
	
	@Override
	public int hashCode()
	{
		return category.hashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof CategoryAction) ) {
			return false;
		}
		final CategoryAction that = (CategoryAction) obj;
		return category == that.category;
	}
	
	@Override
	public String toString()
	{
		return "CategoryAction[" + category + "]";
	}
}
