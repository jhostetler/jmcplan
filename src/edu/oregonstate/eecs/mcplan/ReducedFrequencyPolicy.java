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

/**
 * @author jhostetler
 *
 */
public final class ReducedFrequencyPolicy<S, A extends VirtualConstructor<A>> extends Policy<S, A>
{
	public static interface Criterion<S, A>
	{
		public abstract boolean decisionCheck( final S s );
		public abstract void reset();
	}
	
	public static final class Skip<S, A> implements Criterion<S, A>
	{
		private int t = 0;
		public final int skip;
		
		public Skip( final int skip )
		{
			this.skip = skip;
		}
		
		@Override
		public boolean decisionCheck( final S s )
		{
			final boolean b = (t % skip == 0);
			t += 1;
			return b;
		}
		
		@Override
		public void reset()
		{
			t = 0;
		}
	}
	
	private final Policy<S, A> pi;
	private final A default_action;
	private final Criterion<S, A> criterion;
	
	private boolean decision = false;
	
	public ReducedFrequencyPolicy( final Policy<S, A> pi, final A default_action, final Criterion<S, A> criterion )
	{
		this.pi = pi;
		this.default_action = default_action;
		this.criterion = criterion;
	}
	
	@Override
	public void reset()
	{
		criterion.reset();
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		pi.setState( s, t );
		decision = criterion.decisionCheck( s );
	}

	@Override
	public A getAction()
	{
		if( decision ) {
			return pi.getAction();
		}
		else {
			return default_action.create();
		}
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		if( decision ) {
			pi.actionResult( sprime, r );
		}
	}

	@Override
	public String getName()
	{
		return "ReducedFrequencyPolicy";
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() + 5 * (pi.hashCode() + 7 * (default_action.hashCode()));
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof ReducedFrequencyPolicy<?, ?>) ) {
			return false;
		}
		@SuppressWarnings( "unchecked" )
		final ReducedFrequencyPolicy<S, A> that = (ReducedFrequencyPolicy<S, A>) obj;
		return pi.equals( that.pi ) && default_action.equals( that.default_action )
			   && criterion.equals( that.criterion );
	}

}
