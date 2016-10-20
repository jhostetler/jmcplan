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

import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.NonPositiveDefiniteMatrixException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

/**
 * @author jhostetler
 *
 */
public class MatrixAlgorithms
{
	/**
	 * Computes the inverse of a matrix using the singular value decomposition.
	 * 
	 * The input matrix M is assumed to be positive definite up to numerical
	 * precision 'eps'. That is, for all eigenvalues lambda of M, it must be
	 * the case that lambda + eps > 0. For eigenvalues with |lambda| < eps, the
	 * eigenvalue is set to 'eps' before inverting. Throws an exception if
	 * any lambda < -eps.
	 * @param M
	 * @param eps
	 * @return
	 */
	public static RealMatrix robustInversePSD( final RealMatrix M, final double eps )
	{
		assert( eps > 0.0 );
		final SingularValueDecomposition svd = new SingularValueDecomposition( M );
		final RealMatrix Sigma = svd.getS().copy();
		final int N = Math.min( Sigma.getColumnDimension(), Sigma.getRowDimension() );
		for( int i = 0; i < N; ++i ) {
			final double lambda = Sigma.getEntry( i, i );
			System.out.println( "lambda_" + i + " = " + lambda );
			if( Math.abs( lambda ) < eps ) {
				System.out.println( "Corrected " + i );
				Sigma.setEntry( i, i, 1.0 / eps );
			}
			else if( lambda < 0.0 ) {
				throw new IllegalArgumentException( "Negative eigenvalue " + lambda );
			}
			else {
				Sigma.setEntry( i, i, 1.0 / lambda );
			}
		}
		return svd.getV().multiply( Sigma.transpose() ).multiply( svd.getUT() );
	}
	
	public static RealMatrix makePositiveDefinite( final RealMatrix M, final double eps )
	{
		assert( eps > 0.0 );
		final SingularValueDecomposition svd = new SingularValueDecomposition( M );
		final RealMatrix Sigma = svd.getS().copy();
		final int N = Math.min( Sigma.getColumnDimension(), Sigma.getRowDimension() );
		for( int i = 0; i < N; ++i ) {
			final double lambda = Sigma.getEntry( i, i );
			System.out.println( "lambda_" + i + " = " + lambda );
			if( Math.abs( lambda ) < eps ) {
				System.out.println( "Corrected " + i );
				Sigma.setEntry( i, i, eps );
			}
			else if( lambda < 0.0 ) {
				throw new NonPositiveDefiniteMatrixException( lambda, i, eps );
			}
			else {
				Sigma.setEntry( i, i, lambda );
			}
		}
		return svd.getU().multiply( Sigma ).multiply( svd.getVT() );
	}
	
	public static boolean isPositiveDefinite( final RealMatrix M, final double absolute_positivity_threshold )
	{
		try {
			final CholeskyDecomposition chol = new CholeskyDecomposition(
				M, CholeskyDecomposition.DEFAULT_RELATIVE_SYMMETRY_THRESHOLD, absolute_positivity_threshold );
			return true;
		}
		catch( final NonPositiveDefiniteMatrixException ex ) {
			return false;
		}
	}
}
