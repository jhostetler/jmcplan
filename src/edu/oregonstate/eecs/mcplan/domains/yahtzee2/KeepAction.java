/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.util.Fn;


/**
 * Keep one or more dice, and re-roll the others.
 * 
 * @author jhostetler
 */
public class KeepAction extends YahtzeeAction
{
	public final int[] keepers;
	public final int Nkeepers;
	
	private Hand old_hand_ = null;
	
	public KeepAction( final int[] keepers )
	{
		this.keepers = keepers;
		Nkeepers = Fn.sum( keepers );
		assert( Nkeepers >= 0 );
		// Note: This assertion breaks a heuristic method for excluding
		// the "keep everything" action (see: YahtzeeActionGenerator ctor).
//		assert( Nkeepers < Hand.Ndice );
	}
	
	@Override
	public KeepAction create()
	{
		return new KeepAction( keepers );
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
		
		final int[] r = s.roll( Hand.Ndice - Nkeepers );
		Fn.vplus_inplace( r, keepers );
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
		return "Keep" + Arrays.toString( keepers );
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof KeepAction) ) {
			return false;
		}
		final KeepAction that = (KeepAction) obj;
		return Arrays.equals( keepers, that.keepers );
	}
	
	@Override
	public int hashCode()
	{
		return 5 * Arrays.hashCode( keepers );
	}

}
