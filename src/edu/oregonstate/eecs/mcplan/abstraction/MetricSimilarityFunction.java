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
