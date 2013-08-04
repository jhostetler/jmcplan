/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;

import java.util.ArrayList;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * A simple implementation of k-means clustering for RealVectors. Uses L2
 * distance by default, but you can use a different distance function by
 * overriding 'distance()'.
 */
public class KMeans implements Runnable
{
	private final int k_;
	private final RealVector[] centers_;
	private final RealVector[] data_;
	private final int m_;
	private final int n_;
	private final int[] c_;
	
	public KMeans( final int k, final RealVector[] data )
	{
		k_ = k;
		centers_ = new RealVector[k];
		data_ = data;
		m_ = data_[0].getDimension();
		n_ = data_.length;
		c_ = new int[data_.length];
	}
	
	public double distance( final RealVector a, final RealVector b )
	{
		return a.getDistance( b );
	}
	
	public RealVector[] centers()
	{
		return centers_;
	}
	
	public int[] clusters()
	{
		return c_;
	}
	
	private RealVector centerOfMass( final int c )
	{
		final RealVector com = new ArrayRealVector( m_, 0 );
		int nelements = 0;
		for( int i = 0; i < n_; ++i ) {
			if( c_[i] == c ) {
				nelements += 1;
				com.combineToSelf( 1.0, 1.0, data_[i] ); // Add in-place
			}
		}
		assert( nelements > 0 );
		com.mapDivideToSelf( nelements );
		return com;
	}
	
	private void initCenters()
	{
		final int step = n_ / k_;
		for( int i = 0; i < k_; ++i ) {
			centers_[i] = data_[i * 2].copy();
		}
	}
	
	private void debug()
	{
		System.out.println( "Iteration" );
		for( int i = 0; i < centers().length; ++i ) {
			System.out.println( "Center " + i + ": " + centers()[i] );
			for( int j = 0; j < clusters().length; ++j ) {
				if( clusters()[j] == i ) {
					System.out.println( "\tPoint " + data_[j] );
				}
			}
		}
	}
	
	@Override
	public void run()
	{
		initCenters();
		
		boolean progress = false;
		while( true ) {
			// Expectation
			progress = false;
			for( int i = 0; i < data_.length; ++i ) {
				double d = Double.MAX_VALUE;
				int c = 0;
				for( int j = 0; j < k_; ++j ) {
					final double dp = distance( centers_[j], data_[i] );
					if( dp < d ) {
						d = dp;
						c = j;
					}
				}
				if( c != c_[i] ) {
					c_[i] = c;
					progress = true;
				}
			}
			if( !progress ) {
				break;
			}
			
			// Maximization
			for( int j = 0; j < k_; ++j ) {
				centers_[j] = centerOfMass( j );
			}
			debug();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
		final int nclusters = 2;
		final ArrayList<RealVector> data = new ArrayList<RealVector>();
		
		for( int x = -1; x <= 1; ++x ) {
			for( int y = -1; y <= 1; ++y ) {
				data.add( new ArrayRealVector( new double[] { x, y } ) );
				data.add( new ArrayRealVector( new double[] { x + 10, y + 10} ) );
			}
		}
		
		final KMeans kmeans = new KMeans( nclusters, data.toArray( new RealVector[data.size()] ) ); /* {
			@Override
			public double distance( final RealVector a, final RealVector b ) {
				return a.getL1Distance( b );
			}
		};
		*/
		
		kmeans.run();
		for( int i = 0; i < kmeans.centers().length; ++i ) {
			System.out.println( "Center " + i + ": " + kmeans.centers()[i] );
			for( int j = 0; j < kmeans.clusters().length; ++j ) {
				if( kmeans.clusters()[j] == i ) {
					System.out.println( "\tPoint " + data.get( j ) );
				}
			}
		}
		
	}

}
