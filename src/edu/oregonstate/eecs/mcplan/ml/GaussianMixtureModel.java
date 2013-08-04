/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;

import java.util.ArrayList;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.NonPositiveDefiniteMatrixException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.RandomPermutationIterator;

/**
 * A Gaussian mixture model with a fixed number of components. Training is
 * via EM.
 */
public class GaussianMixtureModel implements Runnable
{
	private final int k_;
	private final double[] pi_;
	private final RealVector[] mu_;
	private final RealMatrix[] Sigma_;
	private final double[][] data_;
	private final int d_;
	private final int n_;
	private final double[][] c_;
	private final double epsilon_;
	private final int max_iterations_ = 20;
	private final RandomGenerator rng_;
	
	private final MultivariateNormalDistribution[] p_;
	private double old_log_likelihood_ = -Double.MAX_VALUE;
	private boolean converged_ = false;
	
	private static final boolean no_copy = false;
	
	public GaussianMixtureModel( final int k, final double[][] data,
								 final double epsilon, final RandomGenerator rng )
	{
		k_ = k;
		pi_ = new double[k_];
		mu_ = new RealVector[k];
		Sigma_ = new RealMatrix[k];
		data_ = data;
		d_ = data_[0].length;
		n_ = data_.length;
		c_ = new double[n_][k_];
		epsilon_ = epsilon;
		rng_ = rng;
		p_ = new MultivariateNormalDistribution[k_];
		
		assert( data_.length > k );
	}
	
	public double distance( final RealVector a, final RealVector b )
	{
		return a.getDistance( b );
	}
	
	public RealVector[] mu()
	{
		return mu_;
	}
	
	public RealMatrix[] Sigma()
	{
		return Sigma_;
	}
	
	public double[][] clusters()
	{
		return c_;
	}
	
	public int nparameters()
	{
		//     pi   mu          Sigma
		return k_ + (k_ * d_) + (k_ * d_ * d_);
	}
	
	public double logLikelihood()
	{
		double l = 0.0;
		for( final double[] x : data_ ) {
			double lx = 0.0;
			for( int i = 0; i < k_; ++i ) {
				lx += posterior( x, i );
			}
			l += Math.log( lx );
		}
		return l;
	}
	
	private void init()
	{
		final int step = Math.max( 1, n_ / k_ );
		final double unif = 1.0 / k_;
		double acc = 0.0;
		final RandomPermutationIterator<double[]> r
			= new RandomPermutationIterator<double[]>( data_, rng_ );
		final RandomPermutationIterator<double[]> rrepeat
			= new RandomPermutationIterator<double[]>( data_, r.permutation() );
		
		for( int i = 0; i < k_; ++i ) {
			final RealVector mu = new ArrayRealVector( d_ );
			for( int j = 0; j < step; ++j ) {
				final double[] x = r.next();
				final RealVector v = new ArrayRealVector( x );
				mu.combineToSelf( 1.0, 1.0, v );
			}
			final double Zinv = 1.0 / step;
			mu.mapMultiplyToSelf( Zinv );
			
			RealMatrix Sigma = new Array2DRowRealMatrix( d_, d_ );
			for( int j = 0; j < step; ++j ) {
				final double[] x = rrepeat.next();
				final RealVector v = new ArrayRealVector( x );
				v.combineToSelf( 1.0, -1.0, mu );
				Sigma = Sigma.add( v.outerProduct( v ) );
			}
			Sigma = Sigma.scalarMultiply( Zinv );
			pi_[i] = unif;
			acc += unif;
			mu_[i] = mu;
			Sigma_[i] = Sigma; //MatrixUtils.createRealIdentityMatrix( d_ );
		}
		pi_[k_ - 1] += (1.0 - acc); // Round-off error
	}
	
	public void debug()
	{
		for( int i = 0; i < mu().length; ++i ) {
			System.out.println( "Pi " + i + ": " + pi_[i] );
			System.out.println( "Mu " + i + ": " + mu()[i] );
			System.out.println( "Sigma " + i + ": " + Sigma_[i] );
			int n = 0;
			for( final double[] x : data_ ) {
				if( mapCluster( x ) == i ) {
//					System.out.println( "\tPoint " + Arrays.toString( x ) );
					n += 1;
				}
			}
			System.out.println( "\t# points = " + n );
		}
	}
	
	public int mapCluster( final double[] x )
	{
		final double[] post = posteriorCluster( x );
		double ml = -Double.MAX_VALUE;
		int mlc = -1;
		for( int i = 0; i < k_; ++i ) {
			if( post[i] > ml ) {
				ml = post[i];
				mlc = i;
			}
		}
		return mlc;
	}
	
	public double[] posteriorCluster( final double[] x )
	{
		final double[] post = new double[k_];
		for( int i = 0; i < k_; ++i ) {
			post[i] = posterior( x, i );
		}
		Fn.normalize_inplace( post );
		return post;
	}
	
	private double posterior( final double[] x, final int c )
	{
//		System.out.println( "posterior( " + Arrays.toString( x ) + ", " + c + " )" );
//		System.out.println( "\tpi_c = " + pi_[c] );
//		System.out.println( "\tp_c = " + mu_[c] + ", " + Sigma_[c] );
		return pi_[c] * p_[c].density( x );
	}
	
	private void fixSigma( final int cluster )
	{
//		final RealMatrix correction = Sigma_[cluster].copy();
//		correction.subtract( Sigma_[cluster].transpose() );
//		correction.scalarMultiply( 0.5 );
//		Sigma_[cluster] = Sigma_[cluster].subtract( correction );
//		System.out.println( "\tafter correction: " + Sigma_[cluster] );
		RealMatrix id = MatrixUtils.createRealIdentityMatrix( d_ );
		double max_diag = -Double.MAX_VALUE;
		for( int i = 0; i < d_; ++i ) {
			final double d = Math.abs( Sigma_[cluster].getEntry( i, i ) );
			if( d > max_diag ) {
				max_diag = d;
			}
		}
//		System.out.println( "\tmax_diag = " + max_diag );
		// FIXME: There's no way to choose the right magnitude for the correction here.
		if( max_diag == 0.0 ) {
			max_diag = 1.0;
		}
//		assert( max_diag > 0 );
		id = id.scalarMultiply( 0.01 * max_diag );
//		System.out.println( "\tid = " + id );
//		System.out.println( "\tbefore correction: " + Sigma_[cluster] );
		Sigma_[cluster] = Sigma_[cluster].add( id );
//		System.out.println( "\tafter correction: " + Sigma_[cluster] );
	}
	
	private void makeMixture()
	{
		for( int i = 0; i < k_; ++i ) {
//			System.out.println( "makeMixture(): component " + i );
			try {
				p_[i] = new MultivariateNormalDistribution( mu_[i].toArray(), Sigma_[i].getData() );
			}
			catch( final SingularMatrixException ex ) {
//				System.out.println( "Fixing Sigma " + i + "(Singular)" );
				fixSigma( i );
				--i;
			}
			catch( final NonPositiveDefiniteMatrixException ex ) {
//				System.out.println( "Fixing Sigma " + i + "(Non-positive definite)" );
				fixSigma( i );
				--i;
			}
		}
	}
	
	private boolean hasConverged( final int c, final double pi, final RealVector mu, final RealMatrix Sigma )
	{
		return (pi - pi_[c]) < epsilon_
			   && mu.getDistance( mu_[c] ) < epsilon_
			   && Sigma.subtract( Sigma_[c] ).getFrobeniusNorm() < epsilon_;
	}
	
	@Override
	public void run()
	{
		init();
		System.out.println( "Init" );
		for( int i = 0; i < mu().length; ++i ) {
			System.out.println( "Mu " + i + ": " + mu()[i] );
			System.out.println( "Sigma " + i + ": " + Sigma()[i] );
		}
		
		int iterations = 0;
		while( !converged_ && iterations++ < max_iterations_ ) {
			// Expectation
			makeMixture();
			for( int i = 0; i < n_; ++i ) {
				for( int j = 0; j < k_; ++j ) {
					c_[i][j] = posterior( data_[i], j );
				}
				Fn.normalize_inplace( c_[i] );
			}
			
			// Maximization
			for( int j = 0; j < k_; ++j ) {
				double Z = 0.0;
				final RealVector mu_j = new ArrayRealVector( d_ );
				RealMatrix Sigma_j = new Array2DRowRealMatrix( d_, d_ );
				for( int i = 0; i < n_; ++i ) {
					final double c_ij = c_[i][j];
					Z += c_ij;
					final RealVector x_i = new ArrayRealVector( data_[i] );
					// mu_j += c_ij * x_i
					mu_j.combineToSelf( 1.0, 1.0, x_i.mapMultiply( c_ij ) );
					final RealVector v = x_i.subtract( mu_[j] );
					// Sigma_j += c_ij * |v><v|
					Sigma_j = Sigma_j.add( v.outerProduct( v ).scalarMultiply( c_ij ) );
				}
				final double Zinv = 1.0 / Z;
				final double pi_j = Z / n_;
				mu_j.mapMultiplyToSelf( Zinv );
				Sigma_j = Sigma_j.scalarMultiply( Zinv );
//				converged &= hasConverged( j, pi_j, mu_j, Sigma_j );
				pi_[j] = pi_j;
				mu_[j] = mu_j;
				Sigma_[j] = Sigma_j;
			}
//			debug();
			
			final double log_likelihood = logLikelihood();
			if( Math.abs( log_likelihood - old_log_likelihood_ ) < epsilon_ ) {
				converged_ = true;
			}
			old_log_likelihood_ = log_likelihood;
		}
	}
	
	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
		final RandomGenerator rng = new MersenneTwister( 42 );
		final ArrayList<double[]> data = new ArrayList<double[]>();
		
		// This data displays some problems with singular covariance estimates,
		// perhaps due to "multicollinearity" in the data.
//		for( int x = -1; x <= 1; ++x ) {
//			for( int y = -1; y <= 1; ++y ) {
//				data.add( new double[] { x, y } );
//				data.add( new double[] { x + 10, y + 10} );
//				data.add( new double[] { x + 20, y + 20} );
//				data.add( new double[] { x + 30, y + 30} );
//			}
//		}
		
		final int nsamples = 1000;
		final double[][] mu = new double[][] {
			new double[] { 0, 0 },
			new double[] { 5, 0 },
			new double[] { 0, 5 },
			new double[] { 5, 5 }
		};
		final double[][] Sigma = new double[][] {
			new double[] { 1, 0 },
			new double[] { 0, 1 }
		};
		final MultivariateNormalDistribution[] p = new MultivariateNormalDistribution[4];
		for( int i = 0; i < 4; ++i ) {
			p[i] = new MultivariateNormalDistribution( rng, mu[i], Sigma );
		}
		for( int i = 0; i < nsamples; ++i ) {
			final int c = rng.nextInt( 4 );
			final double[] x = p[c].sample();
			data.add( x );
		}
		
		// Perturb data
//		for( final double[] x : data ) {
//			for( int i = 0; i < x.length; ++i ) {
//				final double r = rng.nextGaussian() / 1.0;
//				x[i] += r;
//			}
//		}
		
		double best_bic = Double.MAX_VALUE;
		int best_k = 0;
		for( int k = 1; k <= 6; ++k ) {
			System.out.println( "*** k = " + k );
			final GaussianMixtureModel gmm = new GaussianMixtureModel(
				k, data.toArray( new double[data.size()][] ), 10e-5, rng );
			
			gmm.run();
			for( int i = 0; i < gmm.mu().length; ++i ) {
				System.out.println( "Center " + i + ": " + gmm.mu()[i] );
			}
			
			final double bic = ScoreFunctions.bic( data.size(), gmm.nparameters(), gmm.logLikelihood() );
			System.out.println( "BIC = " + bic );
			System.out.println( "ll = " + gmm.logLikelihood() );
			gmm.debug();
			if( bic < best_bic ) {
				best_bic = bic;
				best_k = k;
			}
		}
		System.out.println( "Best model: k = " + best_k );
	}
}
