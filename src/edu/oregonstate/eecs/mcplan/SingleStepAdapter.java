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
 * Adapts a policy over options to return options that always terminate
 * after one step.
 */
public class SingleStepAdapter<S, A> extends Policy<S, Option<S, A>>
{
	private final Policy<S, Option<S, A>> pi_;
	
	private final String str_;
	
	public <P extends Policy<S, Option<S, A>>> SingleStepAdapter( final P pi )
	{
		pi_ = pi;
		str_ = "SingleStepAdapter[" + pi_.getName() + "]";
	}
	
	@Override
	public int hashCode()
	{
		return 53 * pi_.hashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof SingleStepAdapter<?, ?>) ) {
			return false;
		}
		@SuppressWarnings( "unchecked" )
		final SingleStepAdapter<S, A> that = (SingleStepAdapter<S, A>) obj;
		return pi_.equals( that.pi_ );
	}

	@Override
	public void setState( final S s, final long t )
	{
		pi_.setState( s, t );
	}

	@Override
	public Option<S, A> getAction()
	{
		final Option<S, A> o = pi_.getAction();
		return new DurativeAction<S, A>( o.pi(), 1 );
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		pi_.actionResult( sprime, r );
	}

	@Override
	public String getName()
	{
		return str_;
	}
	
	@Override
	public String toString()
	{
		return getName();
	}

}
