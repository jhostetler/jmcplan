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

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.RandomGenerator;

import weka.core.Instance;
import weka.core.Instances;

/**
 * Translation layer between Weka and mcplan algorithms, which mostly use
 * double[] and RealVector for data.
 * 
 * @author jhostetler
 */
public class WekaGlue
{
	public static RealVector toRealVector( final Instance inst )
	{
		final RealVector v = new ArrayRealVector( inst.numAttributes() );
		for( int i = 0; i < inst.numAttributes(); ++i ) {
			v.setEntry( i, inst.value( i ) );
		}
		return v;
	}
	
	public static double[] toDoubleArray( final Instance inst )
	{
		final double[] v = new double[inst.numAttributes()];
		for( int i = 0; i < inst.numAttributes(); ++i ) {
			v[i] = inst.value( i );
		}
		return v;
	}
	
	public static SequentialProjectionHashLearner createSequentialProjectionHashLearner(
		final RandomGenerator rng, final Instances labeled, final Instances unlabeled,
		final int K, final double eta, final double alpha )
	{
		assert( labeled.classIndex() >= 0 );
		final int Nfeatures = labeled.numAttributes() - 1;
		
		final RealMatrix X = new Array2DRowRealMatrix( Nfeatures, labeled.size() + unlabeled.size() );
		final RealMatrix XL = new Array2DRowRealMatrix( Nfeatures, labeled.size() * 2 );
		final RealMatrix S = new Array2DRowRealMatrix( XL.getColumnDimension(), XL.getColumnDimension() );
		
		for( int j = 0; j < labeled.size(); ++j ) {
			final Instance inst = labeled.get( j );
			for( int i = 0; i < XL.getRowDimension(); ++i ) {
				X.setEntry( i, j, inst.value( i ) );
				XL.setEntry( i, j, inst.value( i ) );
			}
			
			int sj = -1;
			Instance s = null;
			do {
				sj = rng.nextInt( labeled.size() );
				s = labeled.get( sj );
			} while( s == inst || s.classValue() != inst.classValue() );
			S.setEntry( j, sj, 1 );
			
			int dj = -1;
			Instance d = null;
			do {
				dj = rng.nextInt( labeled.size() );
				d = labeled.get( dj );
			} while( d == inst || d.classValue() == inst.classValue() );
			S.setEntry( j, dj, -1 );
		}
		
		for( int j = 0; j < unlabeled.size(); ++j ) {
			final Instance inst = unlabeled.get( j );
			for( int i = 0; i < X.getRowDimension(); ++i ) {
				X.setEntry( i, labeled.size() + j, inst.value( i ) );
			}
		}
		
		return new SequentialProjectionHashLearner( X, XL, S, K, eta, alpha );
	}
}
