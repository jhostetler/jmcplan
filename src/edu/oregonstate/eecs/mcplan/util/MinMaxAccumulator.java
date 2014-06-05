/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

/**
 * @author jhostetler
 *
 */
public class MinMaxAccumulator implements StatisticAccumulator
{
	private double min_ = Double.MAX_VALUE;
	private double max_ = -Double.MAX_VALUE;
	
	@Override
	public void add( final double d )
	{
		if( d < min_ ) {
			min_ = d;
		}
		if( d > max_ ) {
			max_ = d;
		}
	}
	
	public double min()
	{
		return min_;
	}
	
	public double max()
	{
		return max_;
	}
}
