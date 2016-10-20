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

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author jhostetler
 *
 */
public class StreamingClusterer
{
	private final SimilarityFunction sf_;
	private final int max_branching_;
	
	private final TIntArrayList clusters_ = new TIntArrayList();
	private final ArrayList<double[]> exemplars_ = new ArrayList<double[]>();
	private final TIntArrayList n_ = new TIntArrayList();
	
	public StreamingClusterer( final SimilarityFunction sf, final int max_branching )
	{
		sf_ = sf;
		max_branching_ = max_branching;
	}
	
	public int clusterState( final double[] phi )
	{
		if( clusters_.size() < max_branching_ ) {
			final int c = clusters_.size();
			clusters_.add( c );
			exemplars_.add( Arrays.copyOf( phi, phi.length ) );
			n_.add( 1 );
			return c;
		}
		else {
			int best_cluster = -1;
			double best_similarity = -Double.MAX_VALUE;
			for( int i = 0; i < clusters_.size(); ++i ) {
				final double[] ex = exemplars_.get( i );
				final double s = sf_.similarity( phi, ex );
				if( s > best_similarity ) {
					best_similarity = s;
					best_cluster = i;
				}
			}
			final int c = clusters_.get( best_cluster );
			final int ni = 1 + n_.get( best_cluster );
			n_.set( best_cluster, ni );
			final double[] ex = exemplars_.get( best_cluster );
			// Modify exemplar to be cluster mean.
			// TODO: Customizable metric / distance calculation?
			for( int j = 0; j < ex.length; ++j ) {
				ex[j] += (phi[j] - ex[j]) / ni;
			}
			return c;
		}
	}
}
