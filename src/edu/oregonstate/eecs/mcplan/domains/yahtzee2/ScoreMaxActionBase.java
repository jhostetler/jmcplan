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
public abstract class ScoreMaxActionBase extends YahtzeeAction
{
	public YahtzeeScores category = null;
	
	private Hand old_hand_ = null;
	private int old_rerolls_ = 0;
	
	private int score_ = 0;
	private int yahtzee_bonus_ = 0;
	
	@Override
	public final void undoAction( final YahtzeeState s )
	{
		assert( old_hand_ != null );
		s.setHand( old_hand_, old_rerolls_ );
		old_hand_ = null;
		old_rerolls_ = 0;
		s.subtractScore( category, score_, yahtzee_bonus_ );
		score_ = yahtzee_bonus_ = 0;
	}

	@Override
	public final void doAction( final RandomGenerator rng, final YahtzeeState s )
	{
		assert( old_hand_ == null );
		
		final Hand h = s.hand();
		old_hand_ = h;
		old_rerolls_ = s.rerolls;
		
		int[] best_value = null;
		int best_scalar = -Integer.MAX_VALUE;
		for( final YahtzeeScores category : YahtzeeScores.values() ) {
			if( filter( category ) && !s.filled[category.ordinal()] ) {
				final int[] value = h.value( s, category );
				final int v = Fn.sum( value );
				if( v > best_scalar ) {
					best_value = value;
					best_scalar = v;
					this.category = category;
				}
			}
		}
		score_ = best_value[0];
		yahtzee_bonus_ = best_value[1];
		assert( !s.filled[category.ordinal()] );
		s.addScore( category, score_, yahtzee_bonus_ );
		
		final int[] r = s.roll( rng, Hand.Ndice );
		s.setHand( new Hand( r ), Hand.Nrerolls );
	}

	@Override
	public final boolean isDone()
	{
		return old_hand_ != null;
	}
	
	protected abstract boolean filter( final YahtzeeScores category );
}
