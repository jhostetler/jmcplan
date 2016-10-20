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

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.Pair;

/**
 * @author jhostetler
 *
 */
public class Datasets
{
	public static Pair<ArrayList<double[]>, int[]> twoVerticalGaussian2D( final RandomGenerator rng, final int Nper_class )
	{
		final int Nclasses = 2;
		final ArrayList<double[]> X = new ArrayList<double[]>();
		final int[] Y = new int[Nclasses * Nper_class];
		
		final double[][] covariance = new double[][] { {0.1*0.1, 0.0},
													   {0.0, 1} };
		final MultivariateNormalDistribution p = new MultivariateNormalDistribution(
			rng, new double[] { 0.0, 0.0 }, covariance );
		
		
		for( int c = 0; c < Nclasses; ++c ) {
			for( int i = 0; i < Nper_class; ++i ) {
				final double[] x = p.sample();
				x[0] += c;
				X.add( x );
				Y[c*Nper_class + i] = c;
			}
		}
		
		return Pair.makePair( X, Y );
	}
}
