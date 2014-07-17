/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.correlation.StorelessCovariance;

import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.util.Csv;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.VectorMeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class LinearDiscriminantAnalysis
{
	public final double[] mean;
	
	public final RealMatrix Sb;
	public final RealMatrix Sw;
	
	public final double[] eigenvalues;
	public final ArrayList<RealVector> eigenvectors;
	
	/**
	 * @param data The elements of 'data' will be modified.
	 * @param label
	 * @param Nclasses
	 * @param shrinkage_intensity
	 */
	public LinearDiscriminantAnalysis( final ArrayList<double[]> data, final int[] label, final int Nclasses,
									   final double shrinkage )
	{
		assert( data.size() == label.length );

		final int Ndata = data.size();
		final int Ndim = data.get( 0 ).length;
		
		// Partition data by class
		final ArrayList<ArrayList<double[]>> classes = new ArrayList<ArrayList<double[]>>( Nclasses );
		for( int i = 0; i < Nclasses; ++i ) {
			classes.add( new ArrayList<double[]>() );
		}
		for( int i = 0; i < data.size(); ++i ) {
			classes.get( label[i] ).add( data.get( i ) );
		}

		// Mean center the data
		
		final VectorMeanVarianceAccumulator mv = new VectorMeanVarianceAccumulator( Ndim );
		for( int i = 0; i < Ndata; ++i ) {
			mv.add( data.get( i ) );
		}
		mean = mv.mean();
		// Subtract global mean
		for( final double[] x : data ) {
			Fn.vminus_inplace( x, mean );
		}
		
		// Calculate class means and covariances
		final double[][] class_mean = new double[Nclasses][Ndim];
		final RealMatrix[] class_cov = new RealMatrix[Nclasses];
		
		for( int i = 0; i < Nclasses; ++i ) {
			final ArrayList<double[]> Xc = classes.get( i );
			final VectorMeanVarianceAccumulator mv_i = new VectorMeanVarianceAccumulator( Ndim );
			final StorelessCovariance cov = new StorelessCovariance( Ndim );
			for( int j = 0; j < Xc.size(); ++j ) {
				final double[] x = Xc.get( j );
				mv_i.add( x );
				cov.increment( x );
			}
			class_mean[i] = mv_i.mean();
			class_cov[i] = cov.getCovarianceMatrix();
		}
		
		// Between-class scatter.
		// Note that 'data' is mean-centered, so the global mean is 0.
		
		RealMatrix Sb_builder = new Array2DRowRealMatrix( Ndim, Ndim );
		for( int i = 0; i < Nclasses; ++i ) {
			final RealVector mu_i = new ArrayRealVector( class_mean[i] );
			final RealMatrix xxt = mu_i.outerProduct( mu_i );
			Sb_builder = Sb_builder.add( xxt.scalarMultiply(
				classes.get( i ).size() / ((double) Ndata - 1) ) );
		}
		this.Sb = Sb_builder;
		Sb_builder = null;
		
		// Within-class scatter with shrinkage estimate:
		// Sw = (1.0 - shrinkage) * \sum Sigma_i + shrinkage * I
		
		RealMatrix Sw_builder = new Array2DRowRealMatrix( Ndim, Ndim );
		for( int i = 0; i < Nclasses; ++i ) {
			final RealMatrix Sigma_i = class_cov[i];
			final RealMatrix scaled = Sigma_i.scalarMultiply(
				(1.0 - shrinkage) * (classes.get( i ).size() - 1) );
			Sw_builder = Sw_builder.add( scaled );
		}
		for( int i = 0; i < Ndim; ++i ) {
			Sw_builder.setEntry( i, i, Sw_builder.getEntry( i, i ) + shrinkage );
		}
		this.Sw = Sw_builder;
		Sw_builder = null;
		
		// Invert Sw
		System.out.println( "[LDA] Sw inverse" );
		final RealMatrix Sw_inv = new LUDecomposition( Sw ).getSolver().getInverse();
		final RealMatrix F = Sw_inv.multiply( Sb );
		
		System.out.println( "[LDA] Eigendecomposition" );
		eigenvalues = new double[Nclasses - 1];
		eigenvectors = new ArrayList<RealVector>( Nclasses - 1 );
		final EigenDecomposition evd = new EigenDecomposition( F );
		for( int j = 0; j < Nclasses - 1; ++j ) {
			final double eigenvalue = evd.getRealEigenvalue( j );
			eigenvalues[j] = eigenvalue;
//			final double scale = 1.0 / Math.sqrt( eigenvalue );
//			eigenvectors.add( evd.getEigenvector( j ).mapMultiply( scale ) );
			eigenvectors.add( evd.getEigenvector( j ) );
		}
	}
	
	public Transformer makeTransformer()
	{
		return new Transformer( this );
	}
	
	public static class Transformer implements VectorSpaceEmbedding<RealVector>
	{
		public final LinearDiscriminantAnalysis lda;
		
		private Transformer( final LinearDiscriminantAnalysis lda )
		{
			this.lda = lda;
		}
		
		/**
		 * Maps a point in flat space to a point in LDA space.
		 * @param x
		 * @return
		 */
		@Override
		public RealVector transform( final RealVector x )
		{
			final RealVector y = new ArrayRealVector( lda.eigenvectors.size() );
			
			for( int i = 0; i < lda.eigenvectors.size(); ++i ) {
				double v = 0.0;
				final RealVector ev = lda.eigenvectors.get( i );
				for( int j = 0; j < x.getDimension(); ++j ) {
					v += ev.getEntry( j ) * (x.getEntry( j ) - lda.mean[j]);
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
			return lda.eigenvectors.size();
		}

		@Override
		public String name()
		{
			return "lda" + lda.eigenvectors.size();
		}
	}
	
	public void writeModel( final File directory )
	{
		try {
			final PrintStream eigenvectors_out = new PrintStream( new FileOutputStream(
				new File( directory, "lda-eigenvectors.csv" ) ) );
			Csv.write( eigenvectors_out, eigenvectors );
			eigenvectors_out.close();
			
			final PrintStream eigenvalues_out = new PrintStream( new FileOutputStream(
				new File( directory, "lda-eigenvalues.csv" ) ) );
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
		final File root = new File( "test/LinearDiscriminantAnalysis" );
		root.mkdirs();
		final int seed = 42;
		final int N = 30;
		final double shrinkage = 1e-6;
		
		final RandomGenerator rng = new MersenneTwister( seed );
		final Pair<ArrayList<double[]>, int[]> dataset = Datasets.twoVerticalGaussian2D( rng, N );
		final ArrayList<double[]> data = dataset.first;
		final int[] label = dataset.second;
		final int Nlabels = 2;
		final int[] shuffle_idx = Fn.linspace( 0, Nlabels*N );
		Fn.shuffle( rng, shuffle_idx );
		final ArrayList<double[]> shuffled = new ArrayList<double[]>();
		final int[] shuffled_label = new int[label.length];
		for( int i = 0; i < data.size(); ++i ) {
			shuffled.add( Fn.copy( data.get( shuffle_idx[i] ) ) );
			shuffled_label[i] = label[shuffle_idx[i]];
		}
		
		final Csv.Writer data_writer = new Csv.Writer( new PrintStream( new File( root, "data.csv" ) ) );
		for( final double[] v : data ) {
			for( int i = 0; i < v.length; ++i ) {
				data_writer.cell( v[i] );
			}
			data_writer.newline();
		}
		data_writer.close();
		
		System.out.println( "[Training]" );
//		final KernelPrincipalComponentsAnalysis<RealVector> kpca
//			= new KernelPrincipalComponentsAnalysis<RealVector>( shuffled, new RadialBasisFunctionKernel( 0.5 ), 1e-6 );
		final LinearDiscriminantAnalysis lda = new LinearDiscriminantAnalysis(
			shuffled, shuffled_label, Nlabels, shrinkage );
		System.out.println( "[Finished]" );
		for( final RealVector ev : lda.eigenvectors ) {
			System.out.println( ev );
		}
		
		System.out.println( "Transformed data:" );
		final LinearDiscriminantAnalysis.Transformer transformer = lda.makeTransformer();
		final Csv.Writer transformed_writer = new Csv.Writer( new PrintStream( new File( root, "transformed.csv" ) ) );
		for( final double[] u : data ) {
			final RealVector uvec = new ArrayRealVector( u );
			System.out.println( uvec );
			final RealVector v = transformer.transform( uvec );
			System.out.println( "-> " + v );
			for( int i = 0; i < v.getDimension(); ++i ) {
				transformed_writer.cell( v.getEntry( i ) );
			}
			transformed_writer.newline();
		}
		transformed_writer.close();
	}

}
