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

package edu.oregonstate.eecs.mcplan.abstraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import weka.core.Instance;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.ml.SimilarityFunction;
import gnu.trove.list.array.TIntArrayList;

/**
 * Represents states by comparing them to the means of clusters created online
 * using a user-specified SimilarityFunction.
 * 
 * Because it constructs partitions online, the resulting representation will
 * depend on the order of samples presented.
 * 
 * @author jhostetler
 *
 * @param <S>
 * @param <X>
 */
public final class PairwiseSimilarityRepresenter<S extends State, X extends FactoredRepresentation<S>>
	implements Representer<S, ClusterAbstraction<S>>
{
	public static abstract class FeatureBuilder
	{
		public abstract Instance makeFeatures( final double[] phi_i, final double[] phi_j );
	}
	
	private final Representer<S, X> repr_;
	private final SimilarityFunction sf_;
	private final double decision_threshold_;
	private final int max_branching_;
	
	private final ArrayList<ClusterAbstraction<S>> clusters_ = new ArrayList<ClusterAbstraction<S>>();
	private final ArrayList<double[]> exemplars_ = new ArrayList<double[]>();
	private final TIntArrayList n_ = new TIntArrayList();
	
	private final HashMap<X, ClusterAbstraction<S>> cache_;
	private final boolean use_cache_ = false;
	
	public PairwiseSimilarityRepresenter( final Representer<S, X> repr,
										  final SimilarityFunction sf,
										  final double decision_threshold,
										  final int max_branching )
	{
		repr_ = repr;
		sf_ = sf;
		decision_threshold_ = decision_threshold;
		max_branching_ = max_branching;
		
		if( use_cache_ ) {
			cache_ = new HashMap<X, ClusterAbstraction<S>>();
		}
		else {
			cache_ = null;
		}
	}
	
	@Override
	public PairwiseSimilarityRepresenter<S, X> create()
	{
		// Note: It is *very* important to call the two-argument
		// Instances constructor, otherwise it takes *forever* even
		// though the Instances object is empty!
		return new PairwiseSimilarityRepresenter<S, X>(
			repr_.create(), sf_, decision_threshold_, max_branching_ );
	}
	
	public ClusterAbstraction<S> clusterState( final double[] phi )
	{
//		System.out.println( "\tcluster " + (count_++) +": size = " + clusters_.size() );
		
		try { // try-block for the sake of Weka
			// TODO: How to do this step is a big design decision. We might
			// eventually like something formally justified, e.g. the
			// "Chinese restaurant process" approach.
			int best_cluster = -1;
			double best_similarity = -Double.MAX_VALUE;
			for( int i = 0; i < clusters_.size(); ++i ) {
				final double[] ex = exemplars_.get( i );
				final double s = sf_.similarity( phi, ex );
//				System.out.println( "similarity: " + s );
				if( s > best_similarity ) {
					best_similarity = s;
					best_cluster = i;
				}
			}
			
//			System.out.println( "*****" );
//			System.out.println( clusters_.size() );
//			System.out.println( max_branching_ );
			if( clusters_.size() == max_branching_ || best_similarity > decision_threshold_ ) {
				final ClusterAbstraction<S> c = clusters_.get( best_cluster );
				final int ni = 1 + n_.get( best_cluster );
				n_.set( best_cluster, ni );
				final double[] ex = exemplars_.get( best_cluster );
				
				// TODO: Debugging
//				System.out.println( Arrays.toString( phi ) + " ~ " + Arrays.toString( ex ) + " (" + c.cluster_ + ")" );
				
				// Modify exemplar to be cluster mean.
				// TODO: Customizable metric / distance calculation?
				for( int j = 0; j < ex.length; ++j ) {
					ex[j] += (phi[j] - ex[j]) / ni;
				}
				return c;
			}
			else {
//				System.out.println( "! " + Arrays.toString( phi ) + " is novel" );
				
				// No match found in loop -> Make new cluster
				final ClusterAbstraction<S> c = new ClusterAbstraction<S>( clusters_.size() );
				clusters_.add( c );
				exemplars_.add( Arrays.copyOf( phi, phi.length ) );
				n_.add( 1 );
				return c;
			}
		}
		catch( final Exception ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	@Override
	public ClusterAbstraction<S> encode( final S s )
	{
		if( s.isTerminal() ) {
			return new ClusterAbstraction<S>( -1 );
		}
		
		// TODO: This needs to know the tree depth, or else you're building a DAG!
		final X x = repr_.encode( s );
		if( use_cache_ ) {
			ClusterAbstraction<S> c = cache_.get( x );
			if( c == null ) {
				c = clusterState( x.phi() );
				cache_.put( x, c );
			}
			return c;
		}
		else {
			return clusterState( x.phi() );
		}
	}
}