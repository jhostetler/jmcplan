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
package edu.oregonstate.eecs.mcplan.rl;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class UcbBandit<T>
{
	public final ArrayList<T> arms;
	public final MeanVarianceAccumulator[] values;
	public final int[] counts;
	public int N = 0;
	
	public final double c;
	
	private static final int nothing_pending = -1;
	private int pending = nothing_pending;
	
	public UcbBandit( final ArrayList<T> arms, final double c )
	{
		assert( arms.size() > 0 );
		this.arms = arms;
		values = new MeanVarianceAccumulator[arms.size()];
		for( int i = 0; i < arms.size(); ++i ) {
			values[i] = new MeanVarianceAccumulator();
		}
		counts = new int[arms.size()];
		
		this.c = c;
	}
	
	/**
	 * Sample an arm index. Each call to sample() must be followed by a call to
	 * update().
	 * @see update(double)
	 * @return
	 */
	public int sample()
	{
		if( pending != nothing_pending ) {
			throw new IllegalStateException( "update() has not been called since last sample()" );
		}
		
		double max_value = -Double.MAX_VALUE;
		for( int i = 0; i < arms.size(); ++i ) {
			if( counts[i] == 0 ) {
				pending = i;
				break;
			}
			else {
				final double exploit = values[i].mean();
				final double explore = c * Math.sqrt( Math.log( N ) / counts[i] );
				final double v = explore + exploit;
				if( v > max_value ) {
					pending = i;
					max_value = v;
				}
			}
		}
		
		assert( pending >= 0 );
		return pending;
	}
	
	/**
	 * Update the value of the last arm sampled.
	 * @see sample()
	 * @param v
	 */
	public void update( final double v )
	{
		if( pending == nothing_pending ) {
			throw new IllegalStateException( "sample() has not been called since last update()" );
		}
		
		values[pending].add( v );
		counts[pending] += 1;
		N += 1;
		
		pending = nothing_pending;
	}
}
