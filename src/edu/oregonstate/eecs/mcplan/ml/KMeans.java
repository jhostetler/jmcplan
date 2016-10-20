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
