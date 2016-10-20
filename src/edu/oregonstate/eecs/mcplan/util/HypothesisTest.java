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
