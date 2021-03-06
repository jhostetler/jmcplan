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

/**
 * @author jhostetler
 *
 */
public class ScoreFunctions
{
	/**
	 * Aikake's information criterion.
	 * @param k Number of parameters
	 * @param log_likelihood
	 * @return
	 */
	public static double aic( final int k, final double log_likelihood )
	{
		return -2 * log_likelihood + 2 * k;
	}
	
	/**
	 * Aikake's information criterion with finite sample correction.
	 * @param n Number of samples
	 * @param k Number of parameters
	 * @param log_likelihood
	 * @return
	 */
	public static double aicc( final int n, final int k, final double log_likelihood )
	{
		assert( n > k + 1 );
		return aic( k, log_likelihood ) + (2 * k * (k + 1)) / (n - k - 1);
	}
	
	/**
	 * Bayesian information criterion.
	 * @param n Number of samples
	 * @param k Number of parameters
	 * @param log_likelihood
	 * @return
	 */
	public static double bic( final int n, final int k, final double log_likelihood )
	{
		return -2 * log_likelihood + k * Math.log( n );
	}
}
