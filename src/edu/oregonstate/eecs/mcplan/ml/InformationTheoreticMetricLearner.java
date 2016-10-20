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
import java.util.Arrays;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * Implements the Information Theoretic Metric Learning algorithm of:
 * 
 * @inproceedings{davis2007information,
 *   title={Information-theoretic metric learning},
 *   author={Davis, Jason V and Kulis, Brian and Jain, Prateek and Sra, Suvrit and Dhillon, Inderjit S},
 *   booktitle={Proceedings of the 24th international conference on Machine learning},
 *   pages={209--216},
 *   year={2007}
 * }
 * 
 * @author jhostetler
 */
public class InformationTheoreticMetricLearner implements Runnable
{
	private final ArrayList<double[]> S_;
	private final ArrayList<double[]> D_;
	public final double u;
	public final double ell;
	private final RealMatrix A0_;
	private final double gamma_;
	private final RandomGenerator rng_;
	
	private final int Nc_;
	private final double[] xi_;
	private final double[] lambda_;
	
	// FIXME: These should be parameters.
	private final double convergence_tolerance_ = 0.01;
	private final int iteration_limit_ = 2000;
	
	private RealMatrix A_ = null;
	private final RealMatrix G_ = null;
	
	/**
	 * @param S A list of differences between Similar pairs of points.
	 * @param D A list of differences between Disimilar pairs of points.
	 * @param u The Upper bound on separation of similar instances.
	 * @param ell The Lower bound on separation of disimilar instances.
	 * @param A0 Initial metric
	 * @param gamma Relative weight of similarity constraints over log-det regularization
	 * @param rng
	 */
	public InformationTheoreticMetricLearner(
		final ArrayList<double[]> S, final ArrayList<double[]> D,
		final double u, final double ell, final RealMatrix A0,
		final double gamma, final RandomGenerator rng )
	{
//		X_ = X;
		S_ = S;
		D_ = D;
		this.u = u;
		this.ell = ell;
		A0_ = A0;
		gamma_ = gamma;
		rng_ = rng;
		
		Nc_ = S_.size() + D_.size();
		xi_ = new double[Nc_];
		Arrays.fill( xi_, 0, S_.size(), u );
		Arrays.fill( xi_, S_.size(), Nc_, ell );
		lambda_ = new double[Nc_];
	}
	
	/**
	 * Returns the learned metric. Valid only after 'run()' has returned.
	 * @return
	 */
	public RealMatrix A()
	{
		return A_;
	}
	
	/**
	 * Execute the algorithm.
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		System.out.println( "\tITML run()" );

		final RealMatrix A = A0_.copy();
		final int[] idx = Fn.range( 0, Nc_ );

		int iter_count = 0;
		final int logging_interval = 1;
		double cumulative_update = 0.0;
		while( iter_count++ < iteration_limit_ ) {
			if( iter_count % logging_interval == 0 ) {
				System.out.println( "\tIteration " + iter_count );
			}
			Fn.shuffle( rng_, idx );
			double update_magnitude = 0.0;
			for( int c = 0; c < Nc_; ++c ) {
				final int[] constraint;
				final RealVector xdiff;
				final int delta;
				if( c < S_.size() ) {
//					constraint = S_.get( c );
					xdiff = new ArrayRealVector( S_.get( c ) );
					delta = 1;
				} else {
//					constraint = D_.get( c - S_.size() );
					xdiff = new ArrayRealVector( D_.get( c - S_.size() ) );
					delta = -1;
				}
				
//				final int i = constraint[0];
//				final int j = constraint[1];
//				final RealVector xdiff = X_.get( i ).subtract( X_.get( j ) );
//				final double p = xdiff.dotProduct( A.operate( xdiff ) );
				
//				if( p == 0.0 ) {
//					System.out.println( "! p == 0.0" );
//				}
//				if( xi_[c] == 0.0 ) {
//					System.out.println( "! xi_[c] == 0.0" );
//				}
				
				final double p = HilbertSpace.inner_prod( xdiff, A, xdiff );
				
				final double alpha;
				if( p == 0.0 ) {
					alpha = lambda_[c];
				}
				else {
					alpha = Math.min( lambda_[c],
									  (delta / 2.0) * ((1.0 / p) - (gamma_ / xi_[c])) );
				}
				final double beta = (delta*alpha) / (1 - delta*alpha*p);
				xi_[c] = (gamma_*xi_[c]) / (gamma_ + delta*alpha*xi_[c]);
				lambda_[c] -= alpha;
				// This implements: A += beta * A xdiff xdiff^T A
				final RealVector Ax = A.operate( xdiff );
//				final RealMatrix outer_prod = Ax.outerProduct( Ax );
				for( int row = 0; row < A.getRowDimension(); ++row ) {
					final double axi = Ax.getEntry( row );
					for( int col = 0; col < A.getColumnDimension(); ++col ) {
						final double a = axi * Ax.getEntry( col );
						A.addToEntry( row, col, a * beta );
					}
				}
//				final RealMatrix outer_prod = xdiff.outerProduct( xdiff );
//				final RealMatrix update = A.multiply( outer_prod ).multiply( A ).scalarMultiply( beta );
//				A = A.add( update );
				update_magnitude += alpha*alpha; //update.getFrobeniusNorm();
			}
			cumulative_update += update_magnitude;
			if( iter_count % logging_interval == 0 ) {
				System.out.println( "\tupdate = " + (cumulative_update / logging_interval) );
				cumulative_update = 0.0;
			}
			// Declare convergence if all updates were small.
			if( Math.sqrt( update_magnitude ) < convergence_tolerance_ ) {
				break;
			}
		}

		A_ = A;
//		A_ = MatrixAlgorithms.makePositiveDefinite( A, 1e-4 );
		
//		System.out.println( "Metric:" );
//		for( int i = 0; i < A_.getRowDimension(); ++i ) {
//			System.out.println( A_.getRowVector( i ) );
//		}
		
		// Check for positive-definiteness
//		final EigenDecomposition eig = new EigenDecomposition( A_ );
//		final double det = eig.getDeterminant();
//		assert( det > 0.0 );
	}
	
	// -----------------------------------------------------------------------
	
	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
		final RandomGenerator rng = new MersenneTwister( 42 );
		final int d = 2;
		final double u = 5.0;
		final double ell = 7.0;
		final double gamma = 1.0;
		final ArrayList<RealVector> X = new ArrayList<RealVector>();
		final RealMatrix A0 = MatrixUtils.createRealIdentityMatrix( d );
		
		for( final int w : new int[] { 0, 5 } ) {
			for( final int h : new int[] { 0, 50 } ) {
				for( int x = -1; x <= 1; ++x ) {
					for( int y = -1; y <= 1; ++y ) {
						X.add( new ArrayRealVector( new double[] { x + w, y + h } ) );
					}
				}
			}
		}
		
		final ArrayList<int[]> S = new ArrayList<int[]>();
		S.add( new int[] { 4, 12 } ); // Must link diagonally
		S.add( new int[] { 21, 31 } );
		final ArrayList<double[]> Sd = new ArrayList<double[]>();
		for( final int[] s : S ) {
			final double[] a = X.get( s[0] ).subtract( X.get( s[1] ) ).toArray();
			Sd.add( a );
		}
		
		final ArrayList<int[]> D = new ArrayList<int[]>();
		D.add( new int[] { 5, 23 } );
		D.add( new int[] { 13, 32 } ); // Cannot link vertically
		final ArrayList<double[]> Dd = new ArrayList<double[]>();
		for( final int[] dd : D ) {
			final double[] a = X.get( dd[0] ).subtract( X.get( dd[1] ) ).toArray();
			Dd.add( a );
		}
		
		final InformationTheoreticMetricLearner itml = new InformationTheoreticMetricLearner(
			Sd, Dd, u, ell, A0, gamma, rng );
		itml.run();
		
		final RealMatrix A = itml.A();
		
		System.out.println( A0.toString() );
		
		for( final int[] c : S ) {
			final RealVector diff = X.get( c[0] ).subtract( X.get( c[1] ) );
			System.out.println( diff.dotProduct( A0.operate( diff ) ) );
		}
		for( final int[] c : D ) {
			final RealVector diff = X.get( c[0] ).subtract( X.get( c[1] ) );
			System.out.println( diff.dotProduct( A0.operate( diff ) ) );
		}
		
		System.out.println( A.toString() );
		
		for( final int[] c : S ) {
			final RealVector diff = X.get( c[0] ).subtract( X.get( c[1] ) );
			System.out.println( diff.dotProduct( A.operate( diff ) ) );
		}
		for( final int[] c : D ) {
			final RealVector diff = X.get( c[0] ).subtract( X.get( c[1] ) );
			System.out.println( diff.dotProduct( A.operate( diff ) ) );
		}
		
//		int i = 0;
//		for( final int w : new int[] { 0, 5 } ) {
//			for( final int h : new int[] { 0, 5 } ) {
//				for( int x = -1; x <= 1; ++x ) {
//					for( int y = -1; y <= 1; ++y ) {
//						System.out.println( itml.A().operate( X.get( i++ ) ) );
//					}
//				}
//			}
//		}
	}
	
}
