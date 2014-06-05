/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;

import org.apache.commons.math3.linear.RealVector;

/**
 * @author jhostetler
 *
 */
public abstract class VoronoiClassifier
{
	private final RealVector[] centers_;
	
	public VoronoiClassifier( final RealVector[] centers )
	{
		centers_ = centers;
	}
	
	protected abstract double distance( final RealVector x1, final RealVector x2 );
	
	public int classify( final RealVector x )
	{
		double best_distance = Double.MAX_VALUE;
		int best_idx = 0;
		for( int i = 0; i < centers_.length; ++i ) {
			final double d = distance( x, centers_[i] );
			if( d < best_distance ) {
				best_distance = d;
				best_idx = i;
			}
		}
//		System.out.print( x );
//		System.out.print( " -> " );
//		System.out.println( centers_[best_idx] );
		return best_idx;
	}
}
