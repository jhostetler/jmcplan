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
