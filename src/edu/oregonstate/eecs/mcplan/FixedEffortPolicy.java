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
 * Adapts an AnytimePolicy into a Policy by always calling it with a fixed
 * 'control' value.
 */
public class FixedEffortPolicy<S, A> extends Policy<S, A>
{
	private final AnytimePolicy<S, A> anytime_;
	private final long control_;
	
	private final String str_;
	
	public FixedEffortPolicy( final AnytimePolicy<S, A> anytime, final long control )
	{
		anytime_ = anytime;
		control_ = control;
		str_ = "FixedEffortPolicy(" + control_ + ")[" + anytime_.getName() + "]";
	}
	
	@Override
	public int hashCode()
	{
		return 61 * anytime_.hashCode() + new Long(control_).hashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof FixedEffortPolicy<?, ?>) ) {
			return false;
		}
		@SuppressWarnings( "unchecked" )
		final FixedEffortPolicy<S, A> that = (FixedEffortPolicy<S, A>) obj;
		return control_ == that.control_ && anytime_.equals( that.anytime_ );
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		anytime_.setState( s, t );
	}

	@Override
	public A getAction()
	{
		return anytime_.getAction( control_ );
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		anytime_.actionResult( sprime, r );
	}

	@Override
	public String getName()
	{
		return str_;
	}
}
