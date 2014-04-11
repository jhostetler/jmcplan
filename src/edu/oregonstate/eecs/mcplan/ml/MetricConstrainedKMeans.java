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
