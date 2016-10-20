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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.distribution.AbstractIntegerDistribution;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public class GibbsDistribution extends AbstractIntegerDistribution
{
	public final double beta;
	
	private final List<Double> xs_ = new ArrayList<Double>();
	private double Z_ = 0.0;
	private double Z_tminus1 = 0.0;
	private double mean_ = 0;
	private double var_ = 0;
	private boolean mv_dirty_ = false;
	
	/**
	 * The Gibbs distribution models the probability of a configuration with
	 * energy x as:
	 * 		P(x) = e^{-\beta x} / Z(\beta)
	 * where Z is the normalizing constant.
	 * 
	 * @param rng
	 * @param beta The "inverse temperature" parameter
	 */
	public GibbsDistribution( final RandomGenerator rng, final double beta )
	{
		super( rng );
		this.beta = beta;
	}
	
	public void add( final double x )
	{
		final double e = Math.exp( -beta * x );
		xs_.add( e );
		Z_tminus1 = Z_;
		Z_ += e;
		mv_dirty_ = true;
	}

	@Override
	public double cumulativeProbability( final int i )
	{
		if( i == xs_.size() - 1 ) {
			return 1.0;
		}
		else {
			final double s = Fn.sum( Fn.slice( xs_, 0, i + 1 ) );
			return s / Z_;
		}
	}

	private void calculateMeanVariance()
	{
		final MeanVarianceAccumulator mv = new MeanVarianceAccumulator();
		for( int i = 0; i < xs_.size(); ++i ) {
			mv.add( i * probability( i ) );
		}
		mean_ = mv.mean();
		var_ = mv.variance();
		mv_dirty_ = false;
	}
	
	@Override
	public double getNumericalMean()
	{
		if( mv_dirty_ ) {
			calculateMeanVariance();
		}
		return mean_;
	}

	@Override
	public double getNumericalVariance()
	{
		if( mv_dirty_ ) {
			calculateMeanVariance();
		}
		return var_;
	}

	@Override
	public int getSupportLowerBound()
	{
		return 0;
	}

	@Override
	public int getSupportUpperBound()
	{
		return xs_.size() - 1;
	}

	@Override
	public int inverseCumulativeProbability( final double p )
			throws OutOfRangeException
	{
		if( p == 0.0 ) {
			return getSupportLowerBound();
		}
		else if( p == 1.0 ) {
			return getSupportUpperBound();
		}
		double s = 0;
		int i = 0;
		for( i = 0; i < xs_.size() - 1; ++i ) {
			final double q = xs_.get( i ) / Z_;
			s += q;
			if( s >= p ) {
				break;
			}
		}
		return i;
	}

	@Override
	public boolean isSupportConnected()
	{
		return true;
	}

	@Override
	public double probability( final int i )
	{
		if( i == getSupportUpperBound() ) {
			return 1 - (Z_tminus1 / Z_);
		}
		else {
			final double p = xs_.get( i ) / Z_;
			return p;
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv )
	{
		final RandomGenerator rng = new MersenneTwister( 42 );
		
		final double[] E = new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		
		final GibbsDistribution low_temp = new GibbsDistribution( rng, 1.0 / 0.01 );
		final GibbsDistribution mid_temp = new GibbsDistribution( rng, 1.0 / 1.0 );
		final GibbsDistribution high_temp = new GibbsDistribution( rng, 1.0 / 100.0 );
		
		for( final double e : E ) {
			low_temp.add( e );
			mid_temp.add( e );
			high_temp.add( e );
		}
		
		final double[] p_low = new double[E.length];
		final double[] p_mid = new double[E.length];
		final double[] p_high = new double[E.length];
		for( int i = 0; i < E.length; ++i ) {
			p_low[i] = low_temp.probability( i );
			p_mid[i] = mid_temp.probability( i );
			p_high[i] = high_temp.probability( i );
		}
		
		System.out.println( "probability():" );
		System.out.println( Arrays.toString( p_low ) );
		System.out.println( Arrays.toString( p_mid ) );
		System.out.println( Arrays.toString( p_high ) );
		
		final int Nsamples = 100000;
		final double[] counts_low = new double[E.length];
		final double[] counts_mid = new double[E.length];
		final double[] counts_high = new double[E.length];
		for( int i = 0; i < Nsamples; ++i ) {
			counts_low[low_temp.sample()] += 1;
			counts_mid[mid_temp.sample()] += 1;
			counts_high[high_temp.sample()] += 1;
		}
		for( int i = 0; i < E.length; ++i ) {
			counts_low[i] /= Nsamples;
			counts_mid[i] /= Nsamples;
			counts_high[i] /= Nsamples;
		}
		
		System.out.println( "Empirical:" );
		System.out.println( Arrays.toString( counts_low ) );
		System.out.println( Arrays.toString( counts_mid ) );
		System.out.println( Arrays.toString( counts_high ) );
	}
}
