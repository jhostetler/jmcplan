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
package edu.oregonstate.eecs.mcplan.ml;

import java.util.ArrayList;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import edu.oregonstate.eecs.mcplan.util.VectorMeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class SequentialProjectionHashLearner implements Runnable, LongHashFunction<RealVector>
{
	public final RealMatrix X;
	public final RealMatrix XL;
	public final RealMatrix S;
	public final int K;
	public final double eta;
	public final double alpha;
	
	private final RealMatrix XLt;
	
	private RealMatrix Xi_;
	private RealMatrix Si_;
	
	public final ArrayList<RealVector> W = new ArrayList<RealVector>();
	public final RealVector b;
	
	private final double shrinkage = 1e-6;
	
	/**
	 * @param X Each column is an unlabeled data point. Will be modified to have 0 mean.
	 * @param XL Each column is a labeled data point. Will be modified to have 0 mean.
	 * @param S S_ij = 1 if XL.get(i) similar to XL.get(j), -1 if not similar, 0 if unknown
	 * @param K Number of hash bits
	 * @param eta Regularization parameter
	 * @param alpha Learning rate (it's more like a boosting weight)
	 */
	public SequentialProjectionHashLearner( final RealMatrix X, final RealMatrix XL, final RealMatrix S,
											final int K, final double eta, final double alpha )
	{
		assert( K <= 64 ); // Because we're going to hash into a long
		
		this.X = X;
		this.XL = XL;
		this.S = S;
		this.K = K;
		this.eta = eta;
		this.alpha = alpha;
		
		Xi_ = X;
		Si_ = S;
		
		final VectorMeanVarianceAccumulator mu = new VectorMeanVarianceAccumulator( X.getRowDimension() );
		for( int j = 0; j < X.getColumnDimension(); ++j ) {
			mu.add( X.getColumn( j ) );
		}
		b = new ArrayRealVector( mu.mean(), false );
		
		for( int i = 0; i < X.getRowDimension(); ++i ) {
			for( int j = 0; j < X.getColumnDimension(); ++j ) {
				final double x = X.getEntry( i, j );
				X.setEntry( i, j, x / b.getEntry( i ) );
			}
		}
		
		for( int i = 0; i < XL.getRowDimension(); ++i ) {
			for( int j = 0; j < XL.getColumnDimension(); ++j ) {
				final double x = XL.getEntry( i, j );
				XL.setEntry( i, j, x / b.getEntry( i ) );
			}
		}
		
		XLt = XL.transpose();
	}
	
	private void T( final RealMatrix S_tilde, final RealMatrix S )
	{
		for( int i = 0; i < S_tilde.getRowDimension(); ++i ) {
			for( int j = 0; j < S_tilde.getColumnDimension(); ++j ) {
				final double st = S_tilde.getEntry( i, j );
				S_tilde.setEntry( i, j, st * S.getEntry( i, j ) < 0 ? st : 0 );
			}
		}
	}
	
	@Override
	public void run()
	{
		final RealMatrix cov_reg = MatrixUtils.createRealIdentityMatrix( X.getRowDimension() ).scalarMultiply( shrinkage );
		for( int k = 0; k < K; ++k ) {
			System.out.println( "k = " + k );
			System.out.println( "\tCovariance" );
			final RealMatrix cov = Xi_.multiply( Xi_.transpose() ).add( cov_reg );
//			System.out.println( cov );
			System.out.println( "\tM" );
			final RealMatrix M = cov; // XL.multiply( Si_ ).multiply( XLt ).add( cov.scalarMultiply( eta ) );
			// TODO: You only need the largest eigenvalue, so the full
			// decomposition is a ton of unnecessary work.
			System.out.println( "\tM[" + M.getRowDimension() + "x" + M.getColumnDimension() + "]" );
			final EigenDecomposition evd = new EigenDecomposition( M );
			final RealVector w = evd.getEigenvector( 0 );
			w.mapMultiplyToSelf( 1.0 / w.getNorm() );
//			if( Math.abs( w.getNorm() - 1.0 ) > 1e-2 ) {
//				System.out.println( "! Non-unit eigenvector: ||w|| = " + w.getNorm() );
//				System.out.println( Math.abs( w.getNorm() - 1.0 ) );
//				assert( false );
//			}
			W.add( w );
			final RealMatrix w_wt = w.outerProduct( w );
			final RealMatrix S_tilde = XLt.multiply( w_wt ).multiply( XL );
			T( S_tilde, Si_ );
			Si_ = Si_.subtract( S_tilde.scalarMultiply( alpha ) );
			Xi_ = Xi_.subtract( w_wt.multiply( Xi_ ) );
		}
	}
	
	@Override
	public long hash( final RealVector x )
	{
		assert( K <= 64 );
		long h = 0L;
		long shift = 1;
		final RealVector centered = x.subtract( b );
		for( int k = 0; k < K; ++k ) {
			final double hk = Math.signum( W.get( k ).dotProduct( centered ) );
			if( hk > 0 ) {
				h |= shift;
			}
			shift <<= 1;
		}
		return h;
	}
	
	public static void main( final String[] argv )
	{
		final double[][] a = new double[][] { { 1.0, 0.0 }, { 0.0, 1.0 } };
		final RealMatrix M = new Array2DRowRealMatrix( a );
		System.out.println( a == M.getData() );
	}
}
