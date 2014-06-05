/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

/**
 * TODO: I *think* it is safe to re-use the same Instances object for multiple
 * different clients of the same SimilarityFunction. Remove this message after
 * you confirm or contradict this.
 * 
 * @author jhostetler
 */
public abstract class ClassifierSimilarityFunction implements SimilarityFunction
{
	private final Classifier classifier_;
	
	// Need an Instances object to add Instance objects to before
	// classification.
	private final Instances dataset_;
	
	public ClassifierSimilarityFunction( final Classifier classifier, final Instances empty_dataset )
	{
		classifier_ = classifier;
		dataset_ = empty_dataset;
	}
	
	public abstract Instance makeFeatures( final double[] a, final double[] b );
	
	@Override
	public double similarity( final double[] a, final double[] b )
	{
		final Instance instance = makeFeatures( a, b );
		dataset_.add( instance );
		instance.setDataset( dataset_ );
		double[] p;
		try {
			p = classifier_.distributionForInstance( instance );
		}
		catch( final Exception ex ) {
			throw new RuntimeException( ex );
		}
		dataset_.remove( 0 );
		assert( p.length == 2 );
		// p[1] = similar
		return p[1];
	}

}
