/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

/**
 * Incremental calculation of mean and variance.
 */
public class MeanVarianceAccumulator implements StatisticAccumulator
{
	private int n_ = 0;
	private double mean_ = 0.0;
	private double m2_ = 0.0;
	
	@Override
	public void add( final double x )
	{
    	n_ += 1;
		final double delta = x - mean_;
		mean_ += (delta / n_);
		m2_ += delta*(x - mean_);
	}
	
	public double mean()
	{
		return mean_;
	}
	
	public double variance()
	{
		if( n_ > 1 ) {
			return m2_ / (n_ - 1);
		}
		else {
			return 0.0;
		}
	}
	
	public double confidence()
	{
		return 1.96 * Math.sqrt( variance() ) / Math.sqrt( n_ );
	}
}
