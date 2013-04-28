/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.AbstractIntegerDistribution;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public class GibbsDistribution extends AbstractIntegerDistribution
{
	private final List<Double> xs_ = new ArrayList<Double>();
	private double Z_ = 0.0;
	private double mean_ = 0;
	private double var_ = 0;
	private boolean mv_dirty_ = false;
	
	public GibbsDistribution( final RandomGenerator rng )
	{
		super( rng );
	}
	
	public void add( final double x )
	{
		final double e = Math.exp( x );
		xs_.add( e );
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
			final double s = F.sum( F.slice( xs_, 0, i + 1 ) );
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
		if( mv_dirty_ = true ) {
			calculateMeanVariance();
		}
		return mean_;
	}

	@Override
	public double getNumericalVariance()
	{
		if( mv_dirty_ = true ) {
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
			return 0;
		}
		else if( p == 1.0 ) {
			return xs_.size() - 1;
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
		final double p = xs_.get( i ) / Z_;
		return p;
	}
}
