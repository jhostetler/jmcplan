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

import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * Exposes the protected variables in FastRandomForest that are necessary for
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
public class NakedFastRandomForest extends FastRandomForest
{
	public NakedFastRfBagging getNakedBagger()
	{
		return (NakedFastRfBagging) m_bagger;
	}
	
	public NakedFastRandomTree[] getNodesForInstance( final Instance inst )
	{
		return getNodesForInstance( inst, Integer.MAX_VALUE );
	}
	
	public NakedFastRandomTree[] getNodesForInstance( final Instance inst, final int max_depth )
	{
		final NakedFastRandomTree[] trees = getNakedBagger().getTrees();
		final NakedFastRandomTree[] nodes = new NakedFastRandomTree[trees.length];
		for( int i = 0; i < m_bagger.getNumIterations(); ++i ) {
			nodes[i] = trees[i].getNodeForInstance( inst, max_depth );
		}
		return nodes;
	}
	
	/**
	 * Builds a classifier for a set of instances.
	 * 
	 * Copy-pasted from FastRandomForest, except that it uses
	 * NakedFastRandomTree as the mother classifier.
	 * 
	 * @param data
	 *            the instances to train the classifier with
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Override
	public void buildClassifier( Instances data ) throws Exception
	{

		// can classifier handle the data?
		getCapabilities().testWithFail( data );

		// remove instances with missing class
		data = new Instances( data );
		data.deleteWithMissingClass();

		// only class? -> build ZeroR model
		if( data.numAttributes() == 1 ) {
			System.err
					.println( "Cannot build model (only class attribute present in data!), "
							+ "using ZeroR model instead!" );
			m_ZeroR = new weka.classifiers.rules.ZeroR();
			m_ZeroR.buildClassifier( data );
			return;
		}
		else {
			m_ZeroR = null;
		}

		/*
		 * Save header with attribute info. Can be accessed later by FastRfTrees
		 * through their m_MotherForest field.
		 */
		m_Info = new Instances( data, 0 );

		m_bagger = new NakedFastRfBagging();

		// Set up the tree options which are held in the motherForest.
		m_KValue = m_numFeatures;
		if( m_KValue > data.numAttributes() - 1 ) m_KValue = data
				.numAttributes() - 1;
		if( m_KValue < 1 ) m_KValue = (int) Utils.log2( data.numAttributes() ) + 1;

		// [jhostetler] This line is the only change from FastRandomForest.buildClassifier
		final FastRandomTree rTree = new NakedFastRandomTree();
		
		rTree.m_MotherForest = this; // allows to retrieve KValue and MaxDepth
		// some temporary arrays which need to be separate for every tree, so
		// that the trees can be trained in parallel in different threads

		// set up the bagger and build the forest
		m_bagger.setClassifier( rTree );
		m_bagger.setSeed( m_randomSeed );
		m_bagger.setNumIterations( m_numTrees );
		m_bagger.setCalcOutOfBag( true );
		m_bagger.setComputeImportances( this.getComputeImportances() );

		((NakedFastRfBagging) m_bagger).buildClassifier( data, m_NumThreads, this );

	}

}
