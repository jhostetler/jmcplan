/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;

import edu.oregonstate.eecs.mcplan.util.Csv;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;
import gnu.trove.list.array.TDoubleArrayList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 */
public class KernelPrincipalComponentsAnalysis<T>
{
	public final ArrayList<T> data;
	
	public final KernelFunction<T> k;
	public final int Ndata;
	
	public final TDoubleArrayList eigenvalues = new TDoubleArrayList();
	public final ArrayList<RealVector> eigenvectors;
	
	private final double[] row_avg;
	private double total_avg = 0.0;
	
	public final double eps = 1e-6;
	
	/**
	 * TODO: Things to consider:
	 * 		* Nystrom approximation to kernel matrix
	 * 		* Iterative eigenvalue algorithm
	 * 		* Online version of KPCA
	 * @param data Training data
	 * @param Nbases Number of eigenvectors to retain
	 * @param k Kernel function
	 * @param jitter We regularize by solving ((1 - jitter)*K + jitter*I).
	 */
	public KernelPrincipalComponentsAnalysis(
		final ArrayList<T> data, final KernelFunction<T> k, final double jitter )
	{
		this.data = data;
		this.k = k;
		this.Ndata = data.size();
		
		// Compute kernel matrix
		System.out.println( "[KPCA] Computing kernel matrix" );
		final RealMatrix K = new Array2DRowRealMatrix( Ndata, Ndata );
		for( int i = 0; i < Ndata; ++i ) {
        	final T xi = data.get( i );
            for( int j = i; j < Ndata; ++j ) {
            	final T xj = data.get( j );
                final double K_ij = (1.0 - jitter) * k.apply( xi, xj );
                final double jitter_if_diag = (i == j ? jitter : 0.0);
                K.setEntry( i, j, K_ij + jitter_if_diag );
                K.setEntry( j, i, K_ij + jitter_if_diag );
            }
		}
//		System.out.println( K );
		
		System.out.println( "[KPCA] Centering" );
		// Averages for centering
		row_avg = new double[Ndata];
		final MeanVarianceAccumulator total_mv = new MeanVarianceAccumulator();
		for( int i = 0; i < Ndata; ++i ) {
			final MeanVarianceAccumulator row_mv = new MeanVarianceAccumulator();
			for( int j = 0; j < Ndata; ++j ) {
				final double K_ij = K.getEntry( i, j );
				row_mv.add( K_ij );
				total_mv.add( K_ij );
			}
			row_avg[i] = row_mv.mean();
		}
		total_avg = total_mv.mean();
		// Centered version of the kernel matrix:
		// K_c(i, j) = K_ij - sum_z K_zj / m - sum_z K_iz / m + sum_{z,y} K_zy / m^2
		for( int i = 0; i < Ndata; ++i ) {
			for( int j = 0; j < Ndata; ++j ) {
				final double K_ij = K.getEntry( i, j );
				K.setEntry( i, j, K_ij - row_avg[i] - row_avg[j] + total_avg );
			}
		}
		
		System.out.println( "[KPCA] Eigendecomposition" );
		eigenvectors = new ArrayList<RealVector>();
		final EigenDecomposition evd = new EigenDecomposition( K );
		for( int j = 0; j < Ndata; ++j ) {
			final double eigenvalue = evd.getRealEigenvalue( j );
			if( eigenvalue < eps ) {
				break;
			}
			eigenvalues.add( eigenvalue );
			final double scale = 1.0 / Math.sqrt( eigenvalue );
			final RealVector eigenvector = evd.getEigenvector( j );
			eigenvectors.add( eigenvector.mapMultiply( scale ) );
		}
	}
	
	public Transformer<T> makeTransformer( final int Nbases )
	{
		return new Transformer<T>( this, Nbases );
	}
	
	public static class Transformer<T> implements VectorSpaceEmbedding<T>
	{
		public final KernelPrincipalComponentsAnalysis<T> kpca;
		public final int Nbases;
		
		private Transformer( final KernelPrincipalComponentsAnalysis<T> kpca, final int Nbases )
		{
			this.kpca = kpca;
			this.Nbases = Nbases;
		}
		
		/**
		 * Maps a point in flat space to a point in KPCA space.
		 * @param x
		 * @return
		 */
		@Override
		public RealVector transform( final T x )
		{
			final RealVector y = new ArrayRealVector( Nbases );
			final RealVector kx = new ArrayRealVector( kpca.Ndata );
			
			final MeanVarianceAccumulator mv = new MeanVarianceAccumulator();
			for( int i = 0; i < kpca.Ndata; ++i ) {
				final T data = kpca.data.get( i );
				final double d = kpca.k.apply( data, x );
				kx.setEntry( i, d );
				mv.add( d );
			}
			final double avg_kx = mv.mean();
			
			for( int i = 0; i < Nbases; ++i ) {
				double v = 0.0;
				final RealVector ev = kpca.eigenvectors.get( i );
				for( int j = 0; j < kpca.Ndata; ++j ) {
					v += ev.getEntry( j )
						 * (kx.getEntry( j ) - avg_kx - kpca.row_avg[i] + kpca.total_avg);
				}
				y.setEntry( i, v );
			}
			
			return y;
		}

//		@Override
//		public int inDimension()
//		{
//			// TODO Auto-generated method stub
//			return 0;
//		}

		@Override
		public int outDimension()
		{
			return Nbases;
		}

		@Override
		public String name()
		{
			return "kpca" + Nbases;
		}
	}
	
	public void writeModel( final File directory, final int Nbases )
	{
		try {
			final PrintStream eigenvectors_out = new PrintStream( new FileOutputStream(
				new File( directory, "kpca-eigenvectors.csv" ) ) );
			System.out.println( "!!! eigenvectors.size() = " + eigenvectors.size() );
			Csv.write( eigenvectors_out, eigenvectors.subList( 0, Nbases ) );
			eigenvectors_out.close();
			
			final PrintStream eigenvalues_out = new PrintStream( new FileOutputStream(
				new File( directory, "kpca-eigenvalues.csv" ) ) );
			Csv.write( eigenvalues_out, eigenvalues );
			eigenvalues_out.close();
		}
		catch( final Exception ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] args ) throws FileNotFoundException
	{
		final File root = new File( "test/KernelPrincipalComponentsAnalysis" );
		root.mkdirs();
		final int seed = 42;
		final int N = 30;
		final RandomGenerator rng = new MersenneTwister( seed );
		final ArrayList<RealVector> data = new ArrayList<RealVector>();
		final ArrayList<RealVector> shuffled = new ArrayList<RealVector>();
		
//		final double[][] covariance = new double[][] { {1.0, 0.0},
//													   {0.0, 1.0} };
//		final MultivariateNormalDistribution p = new MultivariateNormalDistribution(
//			rng, new double[] { 0.0, 0.0 }, covariance );
//		final MultivariateNormalDistribution q = new MultivariateNormalDistribution(
//			rng, new double[] { 10.0, 0.0 }, covariance );
//
//		for( int i = 0; i < N; ++i ) {
//			data.add( new ArrayRealVector( p.sample() ) );
//			data.add( new ArrayRealVector( q.sample() ) );
//		}
//		Fn.shuffle( rng, data );
		
		final double sigma = 0.01;
		final double[][] covariance = new double[][] { {sigma, 0.0},
													   {0.0, sigma} };
		final MultivariateNormalDistribution p = new MultivariateNormalDistribution(
			rng, new double[] { 0.0, 0.0 }, covariance );
		
		for( final double r : new double[] {1.0, 3.0, 5.0} ) {
			for( int i = 0; i < N; ++i ) {
				final double theta = i*2*Math.PI / N;
				final double[] noise = p.sample();
				final RealVector v = new ArrayRealVector( new double[]
					{ r*Math.cos( theta ) + noise[0], r*Math.sin( theta ) + noise[1] } );
				data.add( v );
				shuffled.add( v );
			}
		}
		Fn.shuffle( rng, shuffled );
		
		final Csv.Writer data_writer = new Csv.Writer( new PrintStream( new File( root, "data.csv" ) ) );
		for( final RealVector v : data ) {
			for( int i = 0; i < v.getDimension(); ++i ) {
				data_writer.cell( v.getEntry( i ) );
			}
			data_writer.newline();
		}
		data_writer.close();
		
		System.out.println( "[Training]" );
		final int Ncomponents = 2;
		final KernelPrincipalComponentsAnalysis<RealVector> kpca
			= new KernelPrincipalComponentsAnalysis<RealVector>( shuffled, new RadialBasisFunctionKernel( 0.5 ), 1e-6 );
		System.out.println( "[Finished]" );
		for( int i = 0; i < Ncomponents; ++i ) {
			System.out.println( kpca.eigenvectors.get( i ) );
		}
		
		System.out.println( "Transformed data:" );
		final KernelPrincipalComponentsAnalysis.Transformer<RealVector> transformer = kpca.makeTransformer( Ncomponents );
		final Csv.Writer transformed_writer = new Csv.Writer( new PrintStream( new File( root, "transformed.csv" ) ) );
		for( final RealVector u : data ) {
			final RealVector v = transformer.transform( u );
			System.out.println( v );
			for( int i = 0; i < v.getDimension(); ++i ) {
				transformed_writer.cell( v.getEntry( i ) );
			}
			transformed_writer.newline();
		}
		transformed_writer.close();
	}

}
