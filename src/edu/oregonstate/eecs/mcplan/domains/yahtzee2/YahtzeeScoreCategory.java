/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

/**
 * @author jhostetler
 *
 */
public interface YahtzeeScoreCategory
{
	public boolean isSatisfiedBy( final Hand h );
	
	public int score( final Hand h );
	
	public int maxScore();
	
	public boolean isUpper();
}
