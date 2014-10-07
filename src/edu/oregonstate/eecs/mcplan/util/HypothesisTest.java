/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import java.util.ArrayList;

/**
 * @author jhostetler
 *
 */
public class HypothesisTest
{
	
	/**
	 * @article{zech2003multivariate,
  	 *   title={A multivariate two-sample test based on the concept of minimum energy},
  	 *   author={Zech, Gunter and Aslan, B},
  	 *   journal={Statistical Problems in Particle Physics, Astrophysics, and Cosmology},
  	 *   pages={8--11},
  	 *   year={2003}
	 * }
	 * 
	 * This version uses R(r) = -log r, as suggested in the paper.
	 *
	 * @param X
	 * @param Y
	 * @return
	 */
	public static double energy_2sample( final ArrayList<double[]> X, final ArrayList<double[]> Y )
	{
		double a = 0;
		for( int i = 0; i < X.size(); ++i ) {
			for( int j = i + 1; j < X.size(); ++j ) {
				final double d = Fn.distance_l2( X.get( i ), X.get( j ) );
				if( d > 0 ) {
					a += -Math.log( d );
				}
			}
		}
		
		double b = 0;
		for( int i = 0; i < Y.size(); ++i ) {
			for( int j = i + 1; j < Y.size(); ++j ) {
				final double d = Fn.distance_l2( Y.get( i ), Y.get( j ) );
				if( d > 0 ) {
					b += -Math.log( d );
				}
			}
		}
		
		double c = 0;
		for( int i = 0; i < X.size(); ++i ) {
			for( int j = 0; j < Y.size(); ++j ) {
				final double d = Fn.distance_l2( X.get( i ), Y.get( j ) );
				if( d > 0 ) {
					c += -Math.log( d );
				}
			}
		}
		
		final double Phi = a / (X.size()*X.size())
						 + b / (Y.size()*Y.size())
						 - c / (X.size()*Y.size());
		return Phi;
	}
}
