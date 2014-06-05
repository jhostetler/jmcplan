/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

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
	public void doAction( final YahtzeeState s )
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
			if( h.dice[i] >= 3 ) {
				final int[] keepers = new int[Hand.Nfaces];
				keepers[i] = h.dice[i];
				for( int j = 3; j < Hand.Nfaces; ++j ) {
					keepers[j] = h.dice[j];
				}
				return new KeepAction( keepers );
			}
		}
		case FourOfKind: {
			final int i = Fn.argmax( Fn.reversed( h.dice ) );
			if( h.dice[i] >= 4 ) {
				final int[] keepers = new int[Hand.Nfaces];
				keepers[i] = h.dice[i];
				for( int j = 3; j < Hand.Nfaces; ++j ) {
					keepers[j] = h.dice[j];
				}
				return new KeepAction( keepers );
			}
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
			for( int i = 0; i < Hand.Nfaces; ++i ) {
				if( h.dice[i] >= 3 ) {
					final int[] keepers = new int[Hand.Nfaces];
					keepers[i] = 3;
					return new KeepAction( keepers );
				}
			}
			return new KeepMostAction();
		case Chance:
			if( s.rerolls == 2 ) {
				return new KeepAction( new int[] { 0, 0, 0, 0, h.dice[4], h.dice[5] } );
			}
			else if( s.rerolls == 1 ) {
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
}
