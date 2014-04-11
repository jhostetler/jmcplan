/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;



/**
 * Score the current hand in the given category, then roll a new hand.
 * 
 * @author jhostetler
 */
public class ScoreAction extends YahtzeeAction
{
	public YahtzeeScores category;
	
	private Hand old_hand_ = null;
	private int old_rerolls_ = 0;
	
	private int score_ = 0;
	private int yahtzee_bonus_ = 0;
	
	public ScoreAction( final YahtzeeScores category )
	{
		this.category = category;
	}
	
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
		
		assert( !s.filled[category.ordinal()] );
		if( YahtzeeScores.Yahtzee.isSatisfiedBy( h ) ) {
			// Note: The rules are ambiguous about Yahtzee scoring wrt.
			// Jokers, namely whether you can score a Yahtzee in e.g.
			// ThreeOfKind if you don't have the corresponding Upper section
			// yet. I've decided to allow this, for the sake of consistency.
			// You're still not allowed to score Straight/FullHouse unless
			// you have the upper section.
			if( s.yahtzeeBonusActive() ) {
				yahtzee_bonus_ = 100;
			}
			if( category == YahtzeeScores.Yahtzee ) { // Normal Yahtzee
				score_ = category.score( h );
			}
			else if( category.isSatisfiedBy( h ) ) {
				score_ = category.score( h );
			}
			else if( s.filled[YahtzeeScores.Yahtzee.ordinal()]
					 && s.filled[YahtzeeScores.upper( h.nok_i ).ordinal()] ) { // Scoring Yahtzee as Joker
				score_ = category.score( h );
			}
			else {
				score_ = 0;
			}
		}
		else if( category.isSatisfiedBy( h ) ) {
			score_ = category.score( h );
		}
		else {
			score_ = 0;
		}
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
	public ScoreAction create()
	{
		return new ScoreAction( category );
	}
	
	@Override
	public String toString()
	{
		return "Score[" + category + "]";
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof ScoreAction) ) {
			return false;
		}
		final ScoreAction that = (ScoreAction) obj;
		return category == that.category;
	}
	
	@Override
	public int hashCode()
	{
		return category.hashCode();
	}

}
