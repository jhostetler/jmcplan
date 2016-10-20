/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
	
	public void add( final double x, final int n )
	{
		n_ += n;
		final double delta = x - mean_;
		mean_ += (n*delta / n_);
		m2_ += n*delta*(x - mean_);
	}
	
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
	
	@Override
	public String toString()
	{
		return "{mean: " + mean() + ", var: " + variance() + ", conf: " + confidence() + ", n: " + n() + "}";
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv )
	{
		final RandomGenerator rng = new MersenneTwister( 42 );
		
		final MeanVarianceAccumulator a0 = new MeanVarianceAccumulator();
		final MeanVarianceAccumulator a1 = new MeanVarianceAccumulator();
		final MeanVarianceAccumulator t = new MeanVarianceAccumulator();
		
		final MeanVarianceAccumulator b0 = new MeanVarianceAccumulator();
		final MeanVarianceAccumulator b1 = new MeanVarianceAccumulator();
		
		final int Ni = 10;
		final int Nj = 100;
		for( int i = 0; i < Ni; ++i ) {
			for( int j = 0; j < Nj; ++j ) {
				final double x = i; // rng.nextDouble();
				a0.add( x );
				t.add( x );
			}
			b0.add( i, Nj );
		}
		
		for( int i = 0; i < Ni; ++i ) {
			for( int j = 0; j < Nj; ++j ) {
				final double x = i; // rng.nextDouble();
				a1.add( x );
				t.add( x );
			}
			b1.add( i, Nj );
		}
		
		System.out.println( "a0: " + a0.mean() + " (" + a0.variance() + ")" );
		System.out.println( "a1: " + a1.mean() + " (" + a1.variance() + ")" );
		System.out.println( "t: " + t.mean() + " (" + t.variance() + ")" );
		a0.combine( a1 );
		System.out.println( "combined: " + a0.mean() + " (" + a0.variance() + ")" );
		
		System.out.println( "b0: " + b0.mean() + " (" + b0.variance() + ")" );
		System.out.println( "b1: " + b1.mean() + " (" + b1.variance() + ")" );
	}
}
