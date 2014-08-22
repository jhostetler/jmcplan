/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.Serializable;
import java.util.Arrays;

import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author jhostetler
 *
 */
public class Memorizer implements Classifier, Serializable
{
	/**
	 * Auto-generated.
	 */
	private static final long serialVersionUID = -8682140365845103917L;
	
	private static class ArrayHolder implements Serializable
	{
		/**
		 * Auto-generated
		 */
		private static final long serialVersionUID = -507475972976672859L;
		
		private final double[] a_;
		public ArrayHolder( final double[] a )
		{
			a_ = a;
		}
		
		@Override
		public int hashCode()
		{
			return Arrays.hashCode( a_ );
		}
		
		@Override
		public boolean equals( final Object obj )
		{
			if( !(obj instanceof ArrayHolder) ) {
				return false;
			}
			final ArrayHolder that = (ArrayHolder) obj;
			return Arrays.equals( a_, that.a_ );
		}
	}
	
	private final TObjectIntMap<ArrayHolder> m_ = new TObjectIntHashMap<ArrayHolder>();
	private int class_idx_ = -1;
	private int Nclasses_ = 0;

	@Override
	public void buildClassifier( final Instances data ) throws Exception
	{
		Nclasses_ = data.numClasses();
		class_idx_ = data.classIndex();
		for( final Instance i : data ) {
			final double[] x = i.toDoubleArray();
			final int c = (int) x[class_idx_];
			x[class_idx_] = 0;
			m_.put( new ArrayHolder( x ), c );
		}
	}

	/**
	 * 'instance' must be labeled with the class label in the same position
	 * as in the training instances.
	 * 
	 * @see weka.classifiers.Classifier#classifyInstance(weka.core.Instance)
	 */
	@Override
	public double classifyInstance( final Instance instance ) throws Exception
	{
		assert( instance.classIndex() == class_idx_ );
		final double[] x = instance.toDoubleArray();
		x[class_idx_] = 0;
		return m_.get( new ArrayHolder( x ) );
	}

	/**
	 * 'instance' must be labeled with the class label in the same position
	 * as in the training instances. Returns a degenerate distribution giving
	 * probability 1.0 to the result of 'classifyInstance()'.
	 * 
	 * @see weka.classifiers.Classifier#classifyInstance(weka.core.Instance)
	 */
	@Override
	public double[] distributionForInstance( final Instance instance )
			throws Exception
	{
		final double[] p = new double[Nclasses_];
		final int c = (int) classifyInstance( instance );
		p[c] = 1.0;
		return p;
	}

	@Override
	public Capabilities getCapabilities()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
