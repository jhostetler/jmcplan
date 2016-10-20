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
import weka.core.Utils;

/**
 * Exposes the protected variables in FastRandomTree that are necessary for
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
public class NakedFastRandomTree extends FastRandomTree
{
	public NakedFastRandomForest getMotherForest()
	{
		return (NakedFastRandomForest) m_MotherForest;
	}
	
	private NakedFastRandomTree[] successors_ = null;
	
	public NakedFastRandomTree[] getSuccessors()
	{
		if( successors_ == null ) {
			successors_ = new NakedFastRandomTree[m_Successors.length];
			for( int i = 0; i < m_Successors.length; ++i ) {
				successors_[i] = (NakedFastRandomTree) m_Successors[i];
			}
		}
		return successors_;
	}
	
	public int getAttribute()
	{
		return m_Attribute;
	}
	
	public double getSplitPoint()
	{
		return m_SplitPoint;
	}
	
	public NakedFastRandomTree getNodeForInstance( final Instance i )
	{
		return getNodeForInstance( i, Integer.MAX_VALUE );
	}
	
	public NakedFastRandomTree getNodeForInstance( final Instance i, final int max_depth )
	{
		if( max_depth == 0 ) {
			return this;
		}
		
		if( m_Attribute == -1 ) { // Leaf node
			return this;
		}
		
		if( i.isMissing( m_Attribute ) ) {
			throw new IllegalStateException( "NakedFastRandomTree does not support missing attributes" );
		}
		
		final int next_depth = max_depth - 1;
		final NakedFastRandomTree succ;
		if( m_MotherForest.m_Info.attribute( m_Attribute ).isNominal() ) { // nominal
			// 0.99: new - binary splits (also) for nominal attributes
			if( i.value( m_Attribute ) == m_SplitPoint ) {
				succ = (NakedFastRandomTree) m_Successors[0];
			}
			else {
				succ = (NakedFastRandomTree) m_Successors[1];
			}
		}
		else { // numeric
			if( i.value( m_Attribute ) < m_SplitPoint ) {
				succ = (NakedFastRandomTree) m_Successors[0];
			}
			else {
				succ = (NakedFastRandomTree) m_Successors[1];
			}
		}

		return succ.getNodeForInstance( i, next_depth );
	}

	/**
	 * Recursively generates a tree. A derivative of the buildTree function from
	 * the "weka.classifiers.trees.RandomTree" class, with the following changes
	 * made:
	 * <ul>
	 * 
	 * <li>m_ClassProbs are now remembered only in leaves, not in every node of
	 * the tree
	 * 
	 * <li>m_Distribution has been removed
	 * 
	 * <li>members of dists, splits, props and vals arrays which are not used
	 * are dereferenced prior to recursion to reduce memory requirements
	 * 
	 * <li>a check for "branch with no training instances" is now (FastRF 0.98)
	 * made before recursion; with the current implementation of splitData(),
	 * empty branches can appear only with nominal attributes with more than two
	 * categories
	 * 
	 * <li>each new 'tree' (i.e. node or leaf) is passed a reference to its
	 * 'mother forest', necessary to look up parameters such as maxDepth and K
	 * 
	 * <li>pre-split entropy is not recalculated unnecessarily
	 * 
	 * <li>uses DataCache instead of weka.core.Instances, the reference to the
	 * DataCache is stored as a field in FastRandomTree class and not passed
	 * recursively down new buildTree() calls
	 * 
	 * <li>similarly, a reference to the random number generator is stored in a
	 * field of the DataCache
	 * 
	 * <li>m_ClassProbs are now normalized by dividing with number of instances
	 * in leaf, instead of forcing the sum of class probabilities to 1.0; this
	 * has a large effect when class/instance weights are set by user
	 * 
	 * <li>a little imprecision is allowed in checking whether there was a
	 * decrease in entropy after splitting
	 * 
	 * <li>0.99: the temporary arrays splits, props, vals now are not wide as
	 * the full number of attributes in the dataset (of which only "k" columns
	 * of randomly chosen attributes get filled). Now, it's just a single array
	 * which gets replaced as the k features are evaluated sequentially, but it
	 * gets replaced only if a next feature is better than a previous one.
	 * 
	 * <li>0.99: the SortedIndices are now not cut up into smaller arrays on
	 * every split, but rather re-sorted within the same array in the
	 * splitDataNew(), and passed down to buildTree() as the original large
	 * matrix, but with start and end points explicitly specified
	 * 
	 * </ul>
	 * 
	 * @param sortedIndices
	 *            the indices of the instances of the whole bootstrap replicate
	 * @param startAt
	 *            First index of the instance to consider in this split;
	 *            inclusive.
	 * @param endAt
	 *            Last index of the instance to consider; inclusive.
	 * @param classProbs
	 *            the class distribution
	 * @param debug
	 *            whether debugging is on
	 * @param attIndicesWindow
	 *            the attribute window to choose attributes from
	 * @param depth
	 *            the current depth
	 */
	@Override
	protected void buildTree( int[][] sortedIndices, final int startAt, final int endAt,
			final double[] classProbs, final boolean debug, final int[] attIndicesWindow,
			final int depth )
	{

		m_Debug = debug;
		final int sortedIndicesLength = endAt - startAt + 1;

		// Check if node doesn't contain enough instances or is pure
		// or maximum depth reached, make leaf.
		if( (sortedIndicesLength < Math.max( 2, getMinNum() )) // small
				|| Utils.eq( classProbs[Utils.maxIndex( classProbs )],
						Utils.sum( classProbs ) ) // pure
				|| ((getMaxDepth() > 0) && (depth >= getMaxDepth())) // deep
		) {
			m_Attribute = -1; // indicates leaf (no useful attribute to split
								// on)

			// normalize by dividing with the number of instances (as of ver.
			// 0.97)
			// unless leaf is empty - this can happen with splits on nominal
			// attributes with more than two categories
			if( sortedIndicesLength != 0 ) for( int c = 0; c < classProbs.length; c++ ) {
				classProbs[c] /= sortedIndicesLength;
			}
			m_ClassProbs = classProbs;
			this.data = null;
			return;
		} // (leaf making)

		// new 0.99: all the following are for the best attribute only! they're
		// updated while sequentially through the attributes
		double val = Double.NaN; // value of splitting criterion
		final double[][] dist = new double[2][data.numClasses]; // class distributions
															// (contingency
															// table), indexed
															// first by branch,
															// then by class
		double[] prop = new double[2]; // the branch sizes (as fraction)
		double split = Double.NaN; // split point

		// Investigate K random attributes
		int attIndex = 0;
		int windowSize = attIndicesWindow.length;
		int k = getKValue();
		boolean sensibleSplitFound = false;
		double prior = Double.NaN;
		double bestNegPosterior = -Double.MAX_VALUE;
		int bestAttIdx = -1;

		while( (windowSize > 0) && (k-- > 0 || !sensibleSplitFound) ) {

			final int chosenIndex = data.reusableRandomGenerator.nextInt( windowSize );
			attIndex = attIndicesWindow[chosenIndex];

			// shift chosen attIndex out of window
			attIndicesWindow[chosenIndex] = attIndicesWindow[windowSize - 1];
			attIndicesWindow[windowSize - 1] = attIndex;
			windowSize--;

			// new: 0.99
			final double candidateSplit = distributionSequentialAtt( prop, dist,
					bestNegPosterior, attIndex, sortedIndices[attIndex],
					startAt, endAt );

			if( Double.isNaN( candidateSplit ) ) {
				continue; // we did not improve over a previous attribute!
							// "dist" is unchanged from before
			}
			// by this point we know we have an improvement, so we keep the new
			// split point
			split = candidateSplit;
			bestAttIdx = attIndex;

			if( Double.isNaN( prior ) ) { // needs to be computed only once per
											// branch - is same for all
											// attributes (even regardless of
											// missing values)
				prior = SplitCriteria.entropyOverColumns( dist );
			}

			final double negPosterior = -SplitCriteria
					.entropyConditionedOnRows( dist ); // this is an updated
														// dist
			if( negPosterior > bestNegPosterior ) {
				bestNegPosterior = negPosterior;
			}
			else {
				throw new IllegalArgumentException( "Very strange!" );
			}

			val = prior - (-negPosterior); // we want the greatest reduction in
											// entropy
			if( val > 1e-2 ) { // we allow some leeway here to compensate
				sensibleSplitFound = true; // for imprecision in entropy
											// computation
			}

		} // feature by feature in window

		if( sensibleSplitFound ) {

			m_Attribute = bestAttIdx; // find best attribute
			m_SplitPoint = split;
			m_Prop = prop;
			prop = null; // can be GC'ed

			// int[][][] subsetIndices =
			// new int[dist.length][data.numAttributes][];
			// splitData( subsetIndices, m_Attribute,
			// m_SplitPoint, sortedIndices );
			// int numInstancesBeforeSplit = sortedIndices[0].length;

			final int belowTheSplitStartsAt = splitDataNew( m_Attribute,
					m_SplitPoint, sortedIndices, startAt, endAt );

			m_Successors = new FastRandomTree[dist.length]; // dist.length now
															// always == 2
			for( int i = 0; i < dist.length; i++ ) {
				// [jhostetler] This line is the only difference in this method.
				m_Successors[i] = new NakedFastRandomTree();
				
				m_Successors[i].m_MotherForest = this.m_MotherForest;
				m_Successors[i].data = this.data;
				// new in 0.99 - used in distributionSequentialAtt()
				m_Successors[i].tempDists = this.tempDists;
				m_Successors[i].tempDistsOther = this.tempDistsOther;
				m_Successors[i].tempProps = this.tempProps;

				// check if we're about to make an empty branch - this can
				// happen with
				// nominal attributes with more than two categories (as of ver.
				// 0.98)
				if( belowTheSplitStartsAt - startAt == 0 ) {
					// in this case, modify the chosenAttDists[i] so that it
					// contains
					// the current, before-split class probabilities, properly
					// normalized
					// by the number of instances (as we won't be able to
					// normalize
					// after the split)
					for( int j = 0; j < dist[i].length; j++ )
						dist[i][j] = classProbs[j] / sortedIndicesLength;
				}

				if( i == 0 ) { // before split
					m_Successors[i].buildTree( sortedIndices, startAt,
							belowTheSplitStartsAt - 1, dist[i], m_Debug,
							attIndicesWindow, depth + 1 );
				}
				else { // after split
					m_Successors[i].buildTree( sortedIndices,
							belowTheSplitStartsAt, endAt, dist[i], m_Debug,
							attIndicesWindow, depth + 1 );
				}

				dist[i] = null;

			}
			sortedIndices = null;

		}
		else { // ------ make leaf --------

			m_Attribute = -1;

			// normalize by dividing with the number of instances (as of ver.
			// 0.97)
			// unless leaf is empty - this can happen with splits on nominal
			// attributes
			if( sortedIndicesLength != 0 ) for( int c = 0; c < classProbs.length; c++ ) {
				classProbs[c] /= sortedIndicesLength;
			}

			m_ClassProbs = classProbs;

		}

		this.data = null; // dereference all pointers so data can be GC'd after
							// tree is built

	}

}
