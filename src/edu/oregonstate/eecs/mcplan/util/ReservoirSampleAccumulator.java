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

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Constructs a uniform sample of size 'k' incrementally from an unbounded
 * stream of samples.
 */
public class ReservoirSampleAccumulator<T>
{
	public final int k;
	
	private final RandomGenerator rng_;
	private final ArrayList<T> samples_;
	
	private int n_ = 0;
	
	public ReservoirSampleAccumulator( final RandomGenerator rng, final int k )
	{
		this.k = k;
		rng_ = rng;
		samples_ = new ArrayList<T>( k );
	}
	
	/**
	 * Offer a new sample. Must not be called between 'acceptNext()' and the
	 * corresponding 'addPending()'.
	 * @param t
	 */
	public void add( final T t )
	{
		if( acceptNext() ) {
			addPending( t );
		}
	}
	
	private int pending_ = -1;
	
	/**
	 * Returns true if the next sample is accepted. The client should call
	 * 'addPending()' if and only if acceptNext() returns true.
	 * 
	 * This method together with 'addPending()' allow you to see if an object
	 * would be accepted before constructing it, which is useful if
	 * construction is expensive.
	 * @return
	 */
	public boolean acceptNext()
	{
		assert( pending_ < 0 );
		if( n_ < k ) {
			pending_ = n_;
		}
		else {
			final int j = rng_.nextInt( n_ + 1 );
			if( j < k ) {
				pending_ = j;
			}
			else {
				pending_ = -1;
			}
		}
		if( n_ < Integer.MAX_VALUE - 1 ) {
			n_ += 1;
		}
		return pending_ >= 0;
	}
	
	/**
	 * Add an already-accepted sample. Must be called if and only if the
	 * last call to 'acceptNext()' returned 'true'.
	 * @param t
	 */
	public void addPending( final T t )
	{
		assert( pending_ >= 0 );
		if( n_ <= k ) {
			samples_.add( t );
		}
		else {
			samples_.set( pending_, t );
		}
		pending_ = -1;
	}
	
	/**
	 * The current list of samples. The caller must not modify the list.
	 * @return
	 */
	public ArrayList<T> samples()
	{
		return samples_;
	}
	
	/**
	 * The number of samples seen so far.
	 * @return
	 */
	public int n()
	{
		return n_;
	}
}
