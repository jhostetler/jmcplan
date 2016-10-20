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

package edu.oregonstate.eecs.mcplan;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.util.Fn;


public class RandomPolicy<S, A> extends AnytimePolicy<S, A>
{
	public static <S, A>
	RandomPolicy<S, A> create( final RandomGenerator rng, final ActionGenerator<S, A> action_gen )
	{
		return new RandomPolicy<S, A>( rng, action_gen );
	}
	
	private final ActionGenerator<S, ? extends A> action_gen_;
	private final RandomGenerator rng_;
	
	public RandomPolicy( final RandomGenerator rng, final ActionGenerator<S, ? extends A> action_gen )
	{
		action_gen_ = action_gen;
		rng_ = rng;
	}
	
	@Override
	public int hashCode()
	{
		return 71 * action_gen_.hashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof RandomPolicy<?, ?>) ) {
			return false;
		}
		@SuppressWarnings( "unchecked" )
		final RandomPolicy<S, A> that = (RandomPolicy<S, A>) obj;
		return action_gen_.equals( that.action_gen_ );
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		action_gen_.setState( s, t );
	}

	@Override
	public A getAction()
	{
		final A a = Fn.uniform_choice( rng_, action_gen_ );
		return a;
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{
		return "RandomPolicy";
	}
	
	@Override
	public String toString()
	{
		return getName();
	}

	@Override
	public boolean improvePolicy()
	{
		return false;
	}

}
