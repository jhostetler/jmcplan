package edu.oregonstate.eecs.mcplan.util;

import org.apache.commons.math3.linear.RealVector;

public class VectorMeanVarianceAccumulator
{
	public final int Ndim;
	
	private final MeanVarianceAccumulator[] mv_;
	
	public VectorMeanVarianceAccumulator( final int Ndim )
	{
		this.Ndim = Ndim;
		mv_ = new MeanVarianceAccumulator[Ndim];
		for( int i = 0; i < Ndim; ++i ) {
			mv_[i] = new MeanVarianceAccumulator();
		}
	}
	
	public void add( final double[] x )
	{
		for( int i = 0; i < Ndim; ++i ) {
			mv_[i].add( x[i] );
		}
	}
	
	public void add( final RealVector x )
	{
		for( int i = 0; i < Ndim; ++i ) {
			mv_[i].add( x.getEntry( i ) );
		}
	}
	
	public double[] mean()
	{
		final double[] mu = new double[Ndim];
		for( int i = 0; i < Ndim; ++i ) {
			mu[i] = mv_[i].mean();
		}
		return mu;
	}
	
	public double[] variance()
	{
		final double[] var = new double[Ndim];
		for( int i = 0; i < Ndim; ++i ) {
			var[i] = mv_[i].variance();
		}
		return var;
	}
	
	public double[] confidence()
	{
		final double[] conf = new double[Ndim];
		for( int i = 0; i < Ndim; ++i ) {
			conf[i] = mv_[i].confidence();
		}
		return conf;
	}
}
