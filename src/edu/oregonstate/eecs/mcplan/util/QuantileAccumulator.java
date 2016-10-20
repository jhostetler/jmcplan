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

/**
 * Incrementally computes quantile estimates. This does *not* compute the
 * sample quantiles; it is an estimator of the true quantiles. Note that for
 * small datasets, you're probably better off just sorting the list and
 * computing the sample quantiles.
 * 
 * The algorithm is due to:
 * @article{tierney1983space,
 *   title={A space-efficient recursive procedure for estimating a quantile of an unknown distribution},
 *   author={Tierney, Luke},
 *   journal={SIAM Journal on Scientific and Statistical Computing},
 *   volume={4},
 *   number={4},
 *   pages={706--711},
 *   year={1983}
 * }
 * 
 * I utilize the presentation of:
 * @inproceedings{chen2000incremental,
 *   title={Incremental quantile estimation for massive tracking},
 *   author={Chen, Fei and Lambert, Diane and Pinheiro, Jos{\'e} C},
 *   booktitle={Proceedings of the Sixth {ACM} {SIGKDD} International Conference on Knowledge Discovery and Data Mining},
 *   pages={516--522},
 *   year={2000}
 * }
 * 
 * @author jhostetler
 *
 */
public class QuantileAccumulator implements StatisticAccumulator
{
	public final double[] quantiles;
	public final double[] estimates;
	public final int Nquantiles;
	
	private final double[] f0_;
	private final double[] f_;
	
	private int n_ = 0;
	
	public QuantileAccumulator( final double... quantiles )
	{
		this.quantiles = quantiles;
		Nquantiles = quantiles.length;
		estimates = new double[Nquantiles];
		f0_ = new double[Nquantiles];
		f_ = new double[Nquantiles];
		for( int i = 0; i < Nquantiles; ++i ) {
			// Smoothed uniform initial estimate
			f0_[i] = f_[i] = (quantiles[i] + 1.0) / (1.0 + Nquantiles);
		}
	}
	
	@Override
	public void add( final double x )
	{
		n_ += 1;
		final double cn = 1.0 / Math.sqrt( n_ );
		final double wn = 1.0 / n_;
		for( int i = 0; i < Nquantiles; ++i ) {
			final double e = n_ > 1 ? Math.max( f_[i], f0_[i] / Math.sqrt( n_ - 1 ) )
									: f0_[i];
			final int Is = x <= estimates[i] ? 1 : 0;
			final int If = Math.abs(x - estimates[i]) <= cn ? 1 : 0;
			estimates[i] += (wn / e) * (quantiles[i] - Is);
			f_[i] = (1.0 - wn)*f_[i] + wn*(If / 2*cn);
		}
	}
	
	public int n()
	{
		return n_;
	}
	
	public static void main( final String[] argv )
	{
		final QuantileAccumulator acc = new QuantileAccumulator( 0, 0.25, 0.5, 0.75, 1.0 );
		for( int k = 0; k < 1000; ++k ) {
			for( int i = 1; i <= 100; ++i ) {
				acc.add( i );
			}
		}
		
		for( int i = 0; i < acc.Nquantiles; ++i ) {
			System.out.println( "q( " + acc.quantiles[i] + " ) = " + acc.estimates[i] );
		}
	}
}
