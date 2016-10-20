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
