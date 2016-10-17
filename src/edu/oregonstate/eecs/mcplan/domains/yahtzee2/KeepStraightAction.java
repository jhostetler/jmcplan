/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * Keeps a single copy of every dice value in the current hand.
 * 
 * @author jhostetler
 */
public class KeepStraightAction extends YahtzeeAction
{
	private Hand old_hand_ = null;
	
	public KeepStraightAction()
	{ }
	
	@Override
	public KeepStraightAction create()
	{
		return new KeepStraightAction();
	}

	@Override
	public void undoAction( final YahtzeeState s )
	{
		assert( old_hand_ != null );
		s.setHand( old_hand_, s.rerolls + 1 );
		old_hand_ = null;
	}

	@Override
	public void doAction( final RandomGenerator rng, final YahtzeeState s )
	{
		assert( old_hand_ == null );
		assert( s.rerolls > 0 );
		old_hand_ = s.hand();
		
		int n = 0;
		final int[] keepers = new int[Hand.Nfaces];
		// Keep one each of the middle values (2,3,4,5)
		for( int i = 1; i < Hand.Nfaces - 1; ++i ) {
			if( old_hand_.dice[i] > 0 ) {
				keepers[i] = 1;
				n += 1;
			}
		}
		// Keep only one end value (6 or 1)
		if( old_hand_.dice[5] >= 1 ) {
			keepers[5] = 1;
			n += 1;
		}
		else if( old_hand_.dice[0] >= 1 ) {
			keepers[0] = 1;
			n += 1;
		}
		
		if( n < Hand.Ndice ) {
			final int[] r = s.roll( rng, Hand.Ndice - n );
			Fn.vplus_inplace( keepers, r );
		}
		final Hand h = new Hand( keepers );
		s.setHand( h, s.rerolls - 1 );
	}

	@Override
	public boolean isDone()
	{
		return old_hand_ != null;
	}
	
	@Override
	public String toString()
	{
		return "KeepStraight";
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof KeepStraightAction) ) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode()
	{
		return 51;
	}
}
