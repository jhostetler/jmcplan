/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;

import java.util.ArrayList;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.Pair;

/**
 * @author jhostetler
 *
 */
public class Datasets
{
	public static Pair<ArrayList<double[]>, int[]> twoVerticalGaussian2D( final RandomGenerator rng, final int Nper_class )
	{
		final int Nclasses = 2;
		final ArrayList<double[]> X = new ArrayList<double[]>();
		final int[] Y = new int[Nclasses * Nper_class];
		
		final double[][] covariance = new double[][] { {0.1*0.1, 0.0},
													   {0.0, 1} };
		final MultivariateNormalDistribution p = new MultivariateNormalDistribution(
			rng, new double[] { 0.0, 0.0 }, covariance );
		
		
		for( int c = 0; c < Nclasses; ++c ) {
			for( int i = 0; i < Nper_class; ++i ) {
				final double[] x = p.sample();
				x[0] += c;
				X.add( x );
				Y[c*Nper_class + i] = c;
			}
		}
		
		return Pair.makePair( X, Y );
	}
}
