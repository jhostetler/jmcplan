/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;


/**
 * @author jhostetler
 *
 */
public class ScoreMaxUpperAction extends ScoreMaxActionBase
{
	@Override
	public ScoreMaxUpperAction create()
	{
		return new ScoreMaxUpperAction();
	}
	
	@Override
	public String toString()
	{
		return "ScoreMaxUpperAction";
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof ScoreMaxUpperAction) ) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode()
	{
		return 101;
	}

	@Override
	protected boolean filter( final YahtzeeScores category )
	{
		return category.isUpper();
	}
}
