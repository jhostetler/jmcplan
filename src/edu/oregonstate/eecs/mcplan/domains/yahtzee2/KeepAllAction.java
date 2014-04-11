/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import java.util.Arrays;


/**
 * Keep all dice of a specified value.
 * 
 * @author jhostetler
 */
public class KeepAllAction extends YahtzeeAction
{
	public final int k;
	
	private Hand old_hand_ = null;
	
	public KeepAllAction( final int k )
	{
		this.k = k;
		assert( k >= 1 );
		assert( k <= Hand.Nfaces );
	}
	
	@Override
	public KeepAllAction create()
	{
		return new KeepAllAction( k );
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
		
		final int n = old_hand_.dice[k - 1];
		final int[] r;
		if( n < Hand.Ndice ) {
			r = s.roll( Hand.Ndice - n );
			r[k - 1] += n;
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
		return "KeepAll[" + k + "]";
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof KeepAllAction) ) {
			return false;
		}
		final KeepAllAction that = (KeepAllAction) obj;
		return k == that.k;
	}
	
	@Override
	public int hashCode()
	{
		return 47 * k;
	}
}
