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
