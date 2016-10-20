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

package edu.oregonstate.eecs.mcplan.util;

import java.util.Arrays;

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
	
	public void add( final double[] x, final int n )
	{
		for( int i = 0; i < Ndim; ++i ) {
			mv_[i].add( x[i], n );
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
	
	@Override
	public String toString()
	{
		return Arrays.toString( mv_ );
	}
}
