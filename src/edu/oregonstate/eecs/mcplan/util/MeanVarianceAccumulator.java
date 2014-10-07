/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

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
	
//	public void combine( final double m, final double s, final double n )
//	{
//
//	}
	
	public void combine( final MeanVarianceAccumulator that )
	{
		if( that.n_ == 0 ) {
			return;
		}
		
		if( this.n_ == 0 ) {
			this.n_ = that.n_;
			this.mean_ = that.mean_;
			this.m2_ = that.m2_;
			return;
		}
		
		final double m = (this.n_ * this.mean_ + that.n_ * that.mean_) / (this.n_ + that.n_);
		final double t_this = this.n_ * this.mean_*this.mean_ + this.m2_;
		final double t_that = that.n_ * that.mean_*that.mean_ + that.m2_;
		final double s = (t_this + t_that) / (this.n_ + that.n_) - m*m;
		
		this.n_ = this.n_ + that.n_;
		this.mean_ = m;
		this.m2_ = s*this.n_;
	}
	
	public double mean()
	{
		return mean_;
	}
	
	/**
	 * @return The *unbiased* variance esimate (ie. normalized by n-1)
	 */
	public double variance()
	{
		if( n_ > 1 ) {
			return m2_ / (n_ - 1);
		}
		else {
			return 0.0;
		}
	}
	
	public int n()
	{
		return n_;
	}
	
	public double confidence()
	{
		return 1.96 * Math.sqrt( variance() ) / Math.sqrt( n_ );
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv )
	{
		final RandomGenerator rng = new MersenneTwister( 42 );
		
		final MeanVarianceAccumulator a0 = new MeanVarianceAccumulator();
		final MeanVarianceAccumulator a1 = new MeanVarianceAccumulator();
		final MeanVarianceAccumulator t = new MeanVarianceAccumulator();
		
		for( int i = 0; i < 100; ++i ) {
			final double x = rng.nextDouble();
			a0.add( x );
			t.add( x );
		}
		
		for( int i = 0; i < 100; ++i ) {
			final double x = rng.nextDouble();
			a1.add( x );
			t.add( x );
		}
		
		System.out.println( "a0: " + a0.mean() + " (" + a0.variance() + ")" );
		System.out.println( "a1: " + a1.mean() + " (" + a1.variance() + ")" );
		System.out.println( "t: " + t.mean() + " (" + t.variance() + ")" );
		a0.combine( a1 );
		System.out.println( "combined: " + a0.mean() + " (" + a0.variance() + ")" );
	}
}
