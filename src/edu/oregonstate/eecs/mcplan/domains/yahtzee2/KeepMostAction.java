/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import java.util.Arrays;

/**
 * @author jhostetler
 *
 */
public class KeepMostAction extends YahtzeeAction
{
	private Hand old_hand_ = null;
	
	@Override
	public KeepMostAction create()
	{
		return new KeepMostAction();
	}

	@Override
	public void undoAction( final YahtzeeState s )
	{
		assert( old_hand_ != null );
		s.setHand( old_hand_, s.rerolls + 1 );
		old_hand_ = null;
	}

	@Override
	public void doAction( final YahtzeeState s )
	{
		assert( old_hand_ == null );
		assert( s.rerolls > 0 );
		old_hand_ = s.hand();
		
		int most_n = -1;
		int most_i = 0;
		// Loop from 6 down, to prefer higher-scoring multiples
		for( int i = Hand.Nfaces - 1; i >= 0; --i ) {
			final int n = old_hand_.dice[i];
			if( n > most_n ) {
				most_i = i;
				most_n = n;
			}
		}
		final int[] r;
		if( most_n < Hand.Ndice ) {
			r = s.roll( Hand.Ndice - most_n );
			r[most_i] += most_n;
		}
		else {
			r = Arrays.copyOf( old_hand_.dice, old_hand_.dice.length );
		}
		final Hand h = new Hand( r );
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
		return "KeepMost";
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof KeepMostAction) ) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode()
	{
		return 61;
	}
}
