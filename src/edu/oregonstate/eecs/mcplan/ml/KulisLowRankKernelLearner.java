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
 * @author jhostetler
 *
 * @article{kulis2009low,
 *  title={Low-rank kernel learning with Bregman matrix divergences},
 *  author={Kulis, Brian and Sustik, M{\'a}ty{\'a}s A and Dhillon, Inderjit S},
 *  journal={Journal of Machine Learning Research},
 *  volume={10},
 *  number={Feb},
 *  pages={341--376},
 *  year={2009}
 * }
 */
public class KulisLowRankKernelLearner implements Runnable
{
	private final ArrayList<RealVector> X_;
	private final ArrayList<int[]> S_;
	private final ArrayList<int[]> D_;
	private final double u_;
	private final double ell_;
	private final RealMatrix A0_;
	private final RealMatrix G0_;
	private final int r_;
	private final double gamma_;
	private final RandomGenerator rng_;
	
	private final int Nc_;
	private final double[] xi_;
	private final double[] lambda_;
	
	private final double convergence_tolerance_ = 0.001;
	private final int iteration_limit_ = 10000;
	
	private RealMatrix A_ = null;
	private RealMatrix G_ = null;
	
	public KulisLowRankKernelLearner(
		final ArrayList<RealVector> X, final ArrayList<int[]> S, final ArrayList<int[]> D,
		final double u, final double ell, final RealMatrix A0,
		final double gamma, final RandomGenerator rng )
	{
		X_ = X;
		S_ = S;
		D_ = D;
		u_ = u;
		ell_ = ell;
		A0_ = A0;
		// FIXME: For testing only!
		r_ = 2;
		G0_ = MatrixUtils.createRealIdentityMatrix( X.size() );
		gamma_ = gamma;
		rng_ = rng;
		
		Nc_ = S_.size() + D_.size();
		xi_ = new double[Nc_];
		Arrays.fill( xi_, 0, S_.size(), u_ );
		Arrays.fill( xi_, S_.size(), Nc_, ell_ );
		lambda_ = new double[Nc_];
	}
	
	public RealMatrix A()
	{
		return A_;
	}
	
	private void choleskyUpdate( final int r, final double alpha,
								 final RealVector x, final RealMatrix B )
	{
		final double[] a = new double[r+1];
		final double[] h = new double[r];
		a[0] = alpha;
		for( int i = 0; i < r; ++i ) {
			final double xi = x.getEntry( i );
			double t = 1 + a[i]*xi*xi;
			h[i] = Math.sqrt( t );
			a[i+1] = a[i] / t;
			t = B.getEntry( i, i );
			double s = 0.0;
			B.setEntry( i, i, t*h[i] ); // t == B_ii
			for( int j = i - 1; j >= 0; --j ) {
				s += t*x.getEntry( j + 1 );
				t = B.getEntry( i, j );
				B.setEntry( i, j, (t + a[j+1]*x.getEntry(j)*s)*h[j] ); // t == B_ij
			}
		}
	}
	
	@Override
	public void run()
	{
		System.out.println( "\tKLRKL run()" );
		
		final RealMatrix B = MatrixUtils.createRealIdentityMatrix( r_ );
		final double[] nu = new double[Nc_];
		final int[] idx = Fn.range( 0, Nc_ );
		
		int iter_count = 0;
		while( iter_count++ < iteration_limit_ ) {
			System.out.println( "\tIteration " + iter_count );
			Fn.shuffle( rng_, idx );
			double update_magnitude = 0.0;
			for( int c = 0; c < Nc_; ++c ) {
				final int[] constraint;
				final int delta;
				if( c < S_.size() ) {
					constraint = S_.get( c );
					delta = 1;
				} else {
					constraint = D_.get( c - S_.size() );
					delta = -1;
				}
				final int i = constraint[0];
				final int j = constraint[1];
				final RealVector v = G0_.getRowVector( i ).subtract( G0_.getRowVector( j ) );
				final RealVector w = B.transpose().operate( v );
				final double p = w.dotProduct( w );
//				if( p == 0.0 ) {
//					System.out.println( "! p == 0.0" );
//				}
//				if( xi_[c] == 0.0 ) {
//					System.out.println( "! xi_[c] == 0.0" );
//				}
				final double alpha;
				if( p == 0.0 ) {
					alpha = nu[c];
				}
				else {
					// FIXME: The paper didn't include cannot-link constraints
					// in their pseudocode. I'm NOT HANDLING THEM right now, because
					// to do so would require re-deriving part of the algorithm.
					alpha = Math.min( nu[c],
									  (gamma_ / (1.0 + gamma_)
									    *((1.0 / p) - (1.0 / u_))) );
				}
				nu[c] -= alpha;
				final double beta = alpha / (1.0 - (alpha*p));
				choleskyUpdate( r_, alpha, w, B );
				update_magnitude += alpha*alpha; //update.getFrobeniusNorm();
			}
			// Declare convergence if all updates were small.
			if( Math.sqrt( update_magnitude ) < convergence_tolerance_ ) {
				break;
			}
		}
		
		G_ = G0_.multiply( B );
		A_ = G_.transpose().multiply( G_ );
		System.out.println( "Metric:" );
		System.out.println( A_ );
	}
	
//	@Override
//	public void run()
//	{
//		System.out.println( "\tITML run()" );
//
//		RealMatrix A = A0_.copy();
//		final int[] idx = Fn.range( 0, Nc_ );
//
//		int iter_count = 0;
//		while( iter_count++ < iteration_limit_ ) {
//			System.out.println( "\tIteration " + iter_count );
//			Fn.shuffle( rng_, idx );
//			double update_magnitude = 0.0;
//			for( int c = 0; c < Nc_; ++c ) {
//				final int[] constraint;
//				final int delta;
//				if( c < S_.size() ) {
//					constraint = S_.get( c );
//					delta = 1;
//				} else {
//					constraint = D_.get( c - S_.size() );
//					delta = -1;
//				}
//				final int i = constraint[0];
//				final int j = constraint[1];
//				final RealVector xdiff = X_.get( i ).subtract( X_.get( j ) );
//				final double p = xdiff.dotProduct( A.operate( xdiff ) );
////				if( p == 0.0 ) {
////					System.out.println( "! p == 0.0" );
////				}
////				if( xi_[c] == 0.0 ) {
////					System.out.println( "! xi_[c] == 0.0" );
////				}
//				final double alpha;
//				if( p == 0.0 ) {
//					alpha = lambda_[c];
//				}
//				else {
//					alpha = Math.min( lambda_[c],
//									  (delta / 2.0) * ((1.0 / p) - (gamma_ / xi_[c])) );
//				}
//				final double beta = (delta*alpha) / (1 - delta*alpha*p);
//				xi_[c] = (gamma_*xi_[c]) / (gamma_ + delta*alpha*xi_[c]);
//				lambda_[c] -= alpha;
//				final RealMatrix outer_prod = xdiff.outerProduct( xdiff );
//				final RealMatrix update = A.multiply( outer_prod ).multiply( A ).scalarMultiply( beta );
//				A = A.add( update );
//				update_magnitude += alpha*alpha; //update.getFrobeniusNorm();
//			}
//			// Declare convergence if all updates were small.
//			if( Math.sqrt( update_magnitude ) < convergence_tolerance_ ) {
//				break;
//			}
//		}
//
//		A_ = A;
//		System.out.println( "Metric:" );
//		System.out.println( A_ );
//	}
	
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
			for( final int h : new int[] { 0, 5 } ) {
				for( int x = -1; x <= 1; ++x ) {
					for( int y = -1; y <= 1; ++y ) {
						X.add( new ArrayRealVector( new double[] { x + w, y + h } ) );
					}
				}
			}
		}
		
		final ArrayList<int[]> S = new ArrayList<int[]>();
		S.add( new int[] { 4, 31 } ); // Must link diagonally
		final ArrayList<int[]> D = new ArrayList<int[]>();
		D.add( new int[] { 4, 13 } );
		D.add( new int[] { 22, 31 } );
		D.add( new int[] { 13, 22 } ); // Cannot link vertically
		
		final KulisLowRankKernelLearner itml = new KulisLowRankKernelLearner(
			X, S, D, u, ell, A0, gamma, rng );
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
