/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.RandomGenerator;

import weka.core.Instance;
import weka.core.Instances;

/**
 * Translation layer between Weka and mcplan algorithms, which mostly use
 * double[] and RealVector for data.
 * 
 * @author jhostetler
 */
public class WekaGlue
{
	public static RealVector toRealVector( final Instance inst )
	{
		final RealVector v = new ArrayRealVector( inst.numAttributes() );
		for( int i = 0; i < inst.numAttributes(); ++i ) {
			v.setEntry( i, inst.value( i ) );
		}
		return v;
	}
	
	public static double[] toDoubleArray( final Instance inst )
	{
		final double[] v = new double[inst.numAttributes()];
		for( int i = 0; i < inst.numAttributes(); ++i ) {
			v[i] = inst.value( i );
		}
		return v;
	}
	
	public static SequentialProjectionHashLearner createSequentialProjectionHashLearner(
		final RandomGenerator rng, final Instances labeled, final Instances unlabeled,
		final int K, final double eta, final double alpha )
	{
		assert( labeled.classIndex() >= 0 );
		final int Nfeatures = labeled.numAttributes() - 1;
		
		final RealMatrix X = new Array2DRowRealMatrix( Nfeatures, labeled.size() + unlabeled.size() );
		final RealMatrix XL = new Array2DRowRealMatrix( Nfeatures, labeled.size() * 2 );
		final RealMatrix S = new Array2DRowRealMatrix( XL.getColumnDimension(), XL.getColumnDimension() );
		
		for( int j = 0; j < labeled.size(); ++j ) {
			final Instance inst = labeled.get( j );
			for( int i = 0; i < XL.getRowDimension(); ++i ) {
				X.setEntry( i, j, inst.value( i ) );
				XL.setEntry( i, j, inst.value( i ) );
			}
			
			int sj = -1;
			Instance s = null;
			do {
				sj = rng.nextInt( labeled.size() );
				s = labeled.get( sj );
			} while( s == inst || s.classValue() != inst.classValue() );
			S.setEntry( j, sj, 1 );
			
			int dj = -1;
			Instance d = null;
			do {
				dj = rng.nextInt( labeled.size() );
				d = labeled.get( dj );
			} while( d == inst || d.classValue() == inst.classValue() );
			S.setEntry( j, dj, -1 );
		}
		
		for( int j = 0; j < unlabeled.size(); ++j ) {
			final Instance inst = unlabeled.get( j );
			for( int i = 0; i < X.getRowDimension(); ++i ) {
				X.setEntry( i, labeled.size() + j, inst.value( i ) );
			}
		}
		
		return new SequentialProjectionHashLearner( X, XL, S, K, eta, alpha );
	}
}
