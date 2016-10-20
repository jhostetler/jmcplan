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
package edu.oregonstate.eecs.mcplan;

import org.apache.commons.math3.random.RandomGenerator;


/**
 * @author jhostetler
 *
 */
public class PolicyAction<S, A extends UndoableAction<S>>
	extends UndoableAction<S> implements VirtualConstructor<PolicyAction<S, A>>
{
	private final Policy<S, A> pi_;
	private final long t_;
	private A a_ = null;
	private boolean done_ = false;
	
	/**
	 * FIXME: Providing 't' in the constructor only works if we always execute
	 * the PolicyAction in the same state that we created it.
	 * @param pi
	 * @param t
	 */
	public PolicyAction( final Policy<S, A> pi, final long t )
	{
		pi_ = pi;
		t_ = t;
	}
	
	@Override
	public void doAction( final RandomGenerator rng, final S s )
	{
		assert( !done_ );
		pi_.setState( s, t_ );
		a_ = pi_.getAction();
		a_.doAction( s );
		done_ = true;
	}

	@Override
	public boolean isDone()
	{
		return done_;
	}

	@Override
	public PolicyAction<S, A> create()
	{
		return new PolicyAction<S, A>( pi_, t_ );
	}

	@Override
	public void undoAction( final S s )
	{
		assert( done_ );
		a_.undoAction( s );
		done_ = false;
	}
	
	@Override
	public int hashCode()
	{
		return pi_.hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof PolicyAction<?, ?>) ) {
			return false;
		}
		@SuppressWarnings( "unchecked" )
		final PolicyAction<S, A> that = (PolicyAction<S, A>) obj;
		return pi_.equals( that.pi_ );
	}
	
	@Override
	public String toString()
	{
		return "PolicyAction[" + pi_.toString() + "]";
	}
}
