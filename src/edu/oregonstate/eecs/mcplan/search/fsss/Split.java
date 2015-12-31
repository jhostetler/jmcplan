package edu.oregonstate.eecs.mcplan.search.fsss;

public class Split
{
	public final int attribute;
	public final double value;
	
	public Split( final int attribute, final double value )
	{
		this.attribute = attribute;
		this.value = value;
	}
}