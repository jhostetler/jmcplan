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
	
	public static double inner_prod( final double[] x, final RealMatrix M, final double[] y )
	{
		double s = 0.0;
		for( int i = 0; i < M.getRowDimension(); ++i ) {
			for( int j = 0; j < M.getColumnDimension(); ++j ) {
				s += x[i]*M.getEntry( i, j )*y[j];
			}
		}
		return s;
	}
	
	public static double inner_prod( final RealVector x, final RealVector y )
	{
		return x.dotProduct( y );
	}
	
	public static double inner_prod( final double[] x, final double[] y )
	{
		double s = 0.0;
		for( int i = 0; i < x.length; ++i ) {
			s += x[i]*y[i];
		}
		return s;
	}
}
