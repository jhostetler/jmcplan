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

import edu.oregonstate.eecs.mcplan.Pair;
import gnu.trove.map.TIntObjectMap;

import java.util.ArrayList;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public class MetricConstrainedKMeans extends ConstrainedKMeans
{
	public final RealMatrix metric;
	private double Dmax_ = 1.0;
	
	public MetricConstrainedKMeans( final int k, final int d, final ArrayList<RealVector> X,
			final RealMatrix metric,
			final TIntObjectMap<Pair<int[], double[]>> M,
			final TIntObjectMap<Pair<int[], double[]>> C, final RandomGenerator rng )
	{
		super( k, d, X, M, C, rng );
		this.metric = metric;
	}

	@Override
	public double distance( final RealVector x1, final RealVector x2 )
	{
		final RealVector diff = x1.subtract( x2 );
		return Math.sqrt( HilbertSpace.inner_prod( diff, metric, diff ) );
	}

	@Override
	public double distanceMax()
	{
		return Dmax_;
	}

	@Override
	protected void initializeDistanceFunction()
	{
		double max_distance = 0.0;
		for( int i = 0; i < X_.size(); ++i ) {
			for( int j = i + 1; j < X_.size(); ++j ) {
				final double d = distance( X_.get( i ), X_.get( j ) );
				if( d > max_distance ) {
					max_distance = d;
				}
			}
		}
		Dmax_ = max_distance;
	}

	@Override
	protected boolean updateDistanceFunction()
	{
		return false;
	}

}
