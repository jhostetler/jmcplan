package edu.oregonstate.eecs.mcplan.abstraction;

import org.apache.commons.math3.linear.RealMatrix;

import edu.oregonstate.eecs.mcplan.ml.HilbertSpace;
import edu.oregonstate.eecs.mcplan.ml.SimilarityFunction;
import edu.oregonstate.eecs.mcplan.util.Fn;

class MetricSimilarityFunction implements SimilarityFunction
{
	private final RealMatrix metric_;
	
	public MetricSimilarityFunction( final RealMatrix metric )
	{
		metric_ = metric;
	}
	
	@Override
	public double similarity( final double[] a, final double[] b )
	{
		final double eps = 1e-6;
		final double[] diff = Fn.vminus( a, b );
		final double ip = HilbertSpace.inner_prod( diff, metric_, diff );
//		assert( ip >= -eps );
		if( ip < 0 ) {
			if( ip > -eps ) {
				return 0.0;
			}
			else {
				throw new IllegalStateException( "inner_prod = " + ip );
			}
		}
		return -Math.sqrt( ip );
	}
}
