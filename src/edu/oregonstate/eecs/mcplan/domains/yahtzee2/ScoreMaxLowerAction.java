package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

public class ScoreMaxLowerAction extends ScoreMaxActionBase
{

	@Override
	public ScoreMaxLowerAction create()
	{
		return new ScoreMaxLowerAction();
	}

	@Override
	protected boolean filter( final YahtzeeScores category )
	{
		return !category.isUpper();
	}
	
	@Override
	public String toString()
	{
		return "ScoreMaxLowerAction";
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof ScoreMaxLowerAction) ) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode()
	{
		return 103;
	}
}
