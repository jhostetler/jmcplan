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

import org.apache.commons.math3.random.RandomGenerator;


/**
 * @author jhostetler
 *
 */
public class RandomPermutationIterator<T> extends Generator<T>
{
	private final T[] x_;
	private final int[] permute_;
	
	private int idx_ = 0;
	
	public RandomPermutationIterator( final T[] x, final int[] permute )
	{
		x_ = x;
		permute_ = permute;
	}
	
	public RandomPermutationIterator( final T[] x, final RandomGenerator rng )
	{
		x_ = x;
		permute_ = new int[x_.length];
		for( int i = 0; i < permute_.length; ++i ) {
			permute_[i] = i;
		}
		ListUtil.randomShuffle( rng, permute_ );
	}
	
	public int[] permutation()
	{
		return permute_;
	}
	
	@Override
	public boolean hasNext()
	{
		return idx_ < x_.length;
	}

	@Override
	public T next()
	{
		return x_[permute_[idx_++]];
	}
	
}
