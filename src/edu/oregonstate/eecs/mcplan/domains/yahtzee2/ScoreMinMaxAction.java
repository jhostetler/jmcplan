/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;


/**
 * Scores in the open box with the minimum max score.
 * 
 * @author jhostetler
 *
 */
public class ScoreMinMaxAction extends YahtzeeAction
{
	public YahtzeeScores category = null;
	
	private Hand old_hand_ = null;
	private int old_rerolls_ = 0;
	
	private int score_ = 0;
	private int yahtzee_bonus_ = 0;
	
	@Override
	public void undoAction( final YahtzeeState s )
	{
		assert( old_hand_ != null );
		s.setHand( old_hand_, old_rerolls_ );
		old_hand_ = null;
		old_rerolls_ = 0;
		s.subtractScore( category, score_, yahtzee_bonus_ );
		score_ = yahtzee_bonus_ = 0;
	}

	@Override
	public void doAction( final YahtzeeState s )
	{
		assert( old_hand_ == null );
		
		final Hand h = s.hand();
		old_hand_ = h;
		old_rerolls_ = s.rerolls;
		
		int best_scalar = -1;
		for( final YahtzeeScores category : YahtzeeScores.values() ) {
			if( !s.filled[category.ordinal()] ) {
				final int v = category.maxScore();
				if( v > best_scalar ) {
					best_scalar = v;
					this.category = category;
				}
			}
		}
		final int[] value = h.value( s, category );
		score_ = value[0];
		yahtzee_bonus_ = value[1];
		assert( !s.filled[category.ordinal()] );
		s.addScore( category, score_, yahtzee_bonus_ );
		
		final int[] r = s.roll( Hand.Ndice );
		s.setHand( new Hand( r ), Hand.Nrerolls );
	}

	@Override
	public boolean isDone()
	{
		return old_hand_ != null;
	}

	@Override
	public ScoreMinMaxAction create()
	{
		return new ScoreMinMaxAction();
	}
	
	@Override
	public String toString()
	{
		return "ScoreMinMax";
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof ScoreMinMaxAction) ) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode()
	{
		return 67;
	}
}