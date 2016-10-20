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
package hr.irb.fastRandomForest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * Exposes the protected variables in FastRfBagging that are necessary for
 * implementing RandomForestKernel.
 * 
 * Our strategy for doing this is to created the "NakedFastXXX" variant classes
 * extending the "FastXXX" classes, and then to copy-and-paste every method
 * that creates a "FastXXX" and alter it to create a "NakedFastXXX".
 * 
 * NakedFastRandomTree has to be in 'hr.irb.fastRandomForest' because
 * FastRandomTree is package-private. The other NakedXXX classes are in the
 * same package for uniformity.
 * 
 * @author jhostetler
 */
public class NakedFastRfBagging extends FastRfBagging
{
	/**
	 * Constructor.
	 */
	public NakedFastRfBagging()
	{
		m_Classifier = new NakedFastRandomTree();
		// This would be some work to support becase 'calcOOBError()' is private.
		m_CalcOutOfBag = false;
	}
	
	private NakedFastRandomTree[] m_Trees = null;
	
	/**
	 * Do not alter the returned array.
	 * 
	 * @return
	 */
	public NakedFastRandomTree[] getTrees()
	{
		if( m_Trees == null ) {
			m_Trees = new NakedFastRandomTree[m_Classifiers.length];
			for( int i = 0; i < m_Classifiers.length; ++i ) {
				m_Trees[i] = (NakedFastRandomTree) m_Classifiers[i];
			}
		}
		return m_Trees;
	}

	/**
	 * String describing default classifier.
	 * 
	 * @return the default classifier classname
	 */
	@Override
	protected String defaultClassifierString()
	{
		return "hr.irb.fastRandomForest.NakedFastRfBagging";
	}
	
	@Override
	public void buildClassifier( final Instances data, final int numThreads, final FastRandomForest motherForest ) throws Exception
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Bagging method. Produces DataCache objects with bootstrap samples of the
	 * original data, and feeds them to the base classifier (which can only be a
	 * FastRandomTree).
	 * 
	 * @param data
	 *            The training set to be used for generating the bagged
	 *            classifier.
	 * @param numThreads
	 *            The number of simultaneous threads to use for computation.
	 *            Pass zero (0) for autodetection.
	 * @param motherForest
	 *            A reference to the FastRandomForest object that invoked this.
	 * 
	 * @throws Exception
	 *             if the classifier could not be built successfully
	 */
	public void buildClassifier( Instances data, final int numThreads,
			final NakedFastRandomForest motherForest ) throws Exception
	{

		// can classifier handle the vals?
		getCapabilities().testWithFail( data );

		// remove instances with missing class
		data = new Instances( data );
		data.deleteWithMissingClass();

		if( !(m_Classifier instanceof NakedFastRandomTree) ) throw new IllegalArgumentException(
				"The NakedFastRfBagging class accepts "
						+ "only NakedFastRandomTree as its base classifier." );

		/*
		 * We fill the m_Classifiers array by creating lots of trees with new()
		 * because this is much faster than using serialization to deep-copy the
		 * one tree in m_Classifier - this is what the
		 * super.buildClassifier(data) normally does.
		 */
		m_Classifiers = new Classifier[m_NumIterations];
		for( int i = 0; i < m_Classifiers.length; i++ ) {
			final NakedFastRandomTree curTree = new NakedFastRandomTree();
			// all parameters for training will be looked up in the motherForest
			// (maxDepth, k_Value)
			curTree.m_MotherForest = motherForest;
			// 0.99: reference to these arrays will get passed down all nodes so
			// the array can be re-used
			// 0.99: this array is of size two as now all splits are binary -
			// even categorical ones
			curTree.tempProps = new double[2];
			curTree.tempDists = new double[2][];
			curTree.tempDists[0] = new double[data.numClasses()];
			curTree.tempDists[1] = new double[data.numClasses()];
			curTree.tempDistsOther = new double[2][];
			curTree.tempDistsOther[0] = new double[data.numClasses()];
			curTree.tempDistsOther[1] = new double[data.numClasses()];
			m_Classifiers[i] = curTree;
		}

		// this was SLOW.. takes approx 1/2 time as training the forest
		// afterwards (!!!)
		// super.buildClassifier(data);

		if( m_CalcOutOfBag && (m_BagSizePercent != 100) ) {
			throw new IllegalArgumentException( "Bag size needs to be 100% if "
					+ "out-of-bag error is to be calculated!" );
		}

		// sorting is performed inside this constructor
		final DataCache myData = new DataCache( data );

		final int bagSize = data.numInstances() * m_BagSizePercent / 100;
		final Random random = new Random( m_Seed );

		final boolean[][] inBag = new boolean[m_Classifiers.length][];

		// thread management
		final ExecutorService threadPool = Executors
				.newFixedThreadPool( numThreads > 0 ? numThreads : Runtime
						.getRuntime().availableProcessors() );
		final List<Future<?>> futures = new ArrayList<Future<?>>(
				m_Classifiers.length );

		try {

			for( int treeIdx = 0; treeIdx < m_Classifiers.length; treeIdx++ ) {

				// create the in-bag dataset (and be sure to remember what's in
				// bag)
				// for computing the out-of-bag error later
				final DataCache bagData = myData.resample( bagSize, random );
				bagData.reusableRandomGenerator = bagData
						.getRandomNumberGenerator( random.nextInt() );
				inBag[treeIdx] = bagData.inBag; // store later for OOB error
												// calculation

				// build the classifier
				if( m_Classifiers[treeIdx] instanceof NakedFastRandomTree ) {

					final FastRandomTree aTree = (FastRandomTree) m_Classifiers[treeIdx];
					aTree.data = bagData;

					final Future<?> future = threadPool.submit( aTree );
					futures.add( future );

				}
				else {
					throw new IllegalArgumentException(
							"The FastRfBagging class accepts "
									+ "only NakedFastRandomTree as its base classifier." );
				}

			}

			// make sure all trees have been trained before proceeding
			for( int treeIdx = 0; treeIdx < m_Classifiers.length; treeIdx++ ) {
				futures.get( treeIdx ).get();

			}

			// [jhostetler] 'm_FeatureImportances' and 'computeOOBError()' are
			// private, so we'll just not compute them.

			// calc OOB error?
			// if( getCalcOutOfBag() || getComputeImportances() ) {
			// // m_OutOfBagError = computeOOBError(data, inBag, threadPool);
			// m_OutOfBagError = computeOOBError( myData, inBag, threadPool );
			// }
			// else {
			// m_OutOfBagError = 0;
			// }

			// // calc feature importances
			// m_FeatureImportances = null;
			// // m_FeatureNames = null;
			// if( getComputeImportances() ) {
			// m_FeatureImportances = new double[data.numAttributes()];
			// // /m_FeatureNames = new String[data.numAttributes()];
			// // Instances dataCopy = new Instances(data); //To scramble
			// // int[] permutation =
			// // FastRfUtils.randomPermutation(data.numInstances(), random);
			// for( int j = 0; j < data.numAttributes(); j++ ) {
			// if( j != data.classIndex() ) {
			// // double sError =
			// // computeOOBError(FastRfUtils.scramble(data, dataCopy,
			// // j, permutation), inBag, threadPool);
			// // double sError = computeOOBError(data, inBag,
			// // threadPool, j, 0);
			// final float[] unscrambled = myData.scrambleOneAttribute( j,
			// random );
			// final double sError = computeOOBError( myData, inBag,
			// threadPool );
			// myData.vals[j] = unscrambled; // restore the original
			// // state
			// m_FeatureImportances[j] = sError - m_OutOfBagError;
			// }
			// // m_FeatureNames[j] = data.attribute(j).name();
			// }
			// }

			threadPool.shutdown();

		}
		finally {
			threadPool.shutdownNow();
		}
	}

}
