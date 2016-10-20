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
 * Adapts a Policy over Options into a Policy over primitive actions, by
 * forwarding calls to Option.pi.
 */
public class OptionPolicy<S, A> extends Policy<S, A>
{
	public final Policy<S, Option<S, A>> mu;
	public Option<S, A> o = null;
	
	private final RandomGenerator rng_;
	private S s_ = null;
	private long t_ = 0L;
	
	/**
	 * Create an OptionPolicy without an RNG. If you use this constructor,
	 * all Options returned by 'mu' *must* have deterministic 'terminate()'
	 * functions (ie. they return 0.0 or 1.0 always). Otherwise, OptionPolicy
	 * will try to generate a random number, resulting in a NullPointException.
	 * @param mu
	 */
	public OptionPolicy( final Policy<S, Option<S, A>> mu )
	{
		this.mu = mu;
		rng_ = null;
	}
	
	public OptionPolicy( final Policy<S, Option<S, A>> mu, final RandomGenerator rng )
	{
		this.mu = mu;
		rng_ = rng;
	}
	
	@Override
	public int hashCode()
	{
		return 67 * mu.hashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof OptionPolicy<?, ?>) ) {
			return false;
		}
		@SuppressWarnings( "unchecked" )
		final OptionPolicy<S, A> that = (OptionPolicy<S, A>) obj;
		return mu.equals( that.mu );
	}
	
	private boolean terminate( final S s, final long t, final Option<S, A> o )
	{
		final double beta = o.terminate( s, t );
		if( beta == 1.0 ) {
			return true;
		}
		else if( beta == 0.0 ) {
			return false;
		}
		else {
			return rng_.nextDouble() < beta;
		}
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		s_ = s;
		t_ = t;
	}

	@Override
	public A getAction()
	{
		// NOTE: We're assuming here that options can't terminate in their
		// initial state. This is a necessary assumption to implement
		// anytime behavior correctly, since we need to have at most one
		// 'getAction()' computation.
		if( o == null || terminate( s_, t_, o ) ) {
			System.out.println( "! Terminated" );
			mu.setState( s_, t_ );
			o = mu.getAction();
			o.start( s_, t_ );
		}
		System.out.println( getName() );
		o.pi().setState( s_, t_ );
		return o.pi().getAction();
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		o.pi().actionResult( sprime, r );
	}

	@Override
	public String getName()
	{
		return "OptionPolicy[" + o.pi().getName() + "]";
	}
}
