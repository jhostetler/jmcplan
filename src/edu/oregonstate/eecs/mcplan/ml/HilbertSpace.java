/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * @author jhostetler
 *
 */
public class HilbertSpace
{
	public static double inner_prod( final RealVector x, final RealMatrix M, final RealVector y )
	{
		// return x.dotProduct( M.operate( y ) );
		double s = 0.0;
		for( int i = 0; i < M.getRowDimension(); ++i ) {
			for( int j = 0; j < M.getColumnDimension(); ++j ) {
				s += x.getEntry( i )*M.getEntry( i, j )*y.getEntry( j );
			}
		}
		return s;
	}
}