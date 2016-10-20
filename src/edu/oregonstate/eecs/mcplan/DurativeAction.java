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
public class DurativeAction<S, A> extends Option<S, A>
{
	public final int T;
	private long start_time_ = 0;
	
	private final String str_;
	
	public DurativeAction( final Policy<S, A> pi, final int T )
	{
		super( pi );
		this.T = T;
		assert( T > 0 );
		str_ = "DurativeAction(" + T + ")[" + pi.getName() + "]";
	}

	@Override
	public void start( final S s, final long t )
	{
		this.start_time_ = t;
//		System.out.println( "Option " + pi.getName() + " started" );
	}

	@Override
	public double terminate( final S s, final long t )
	{
//		System.out.println( "Terminate check " + (t - this.t) );
		return ((t - this.start_time_) >= T ? 1.0 : 0.0);
	}

	@Override
	public Option<S, A> create()
	{
		return new DurativeAction<S, A>( pi, T );
	}

	@Override
	public void setState( final S s, final long t )
	{
		pi.setState( s, t );
	}

	@Override
	public A getAction()
	{
		return pi.getAction();
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		// TODO: Accumulate option reward?
		pi.actionResult( sprime, r );
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
	
	@Override
	public int hashCode()
	{
		final int k = 109;
		int h = 101;
		h = h*k + pi.hashCode();
		h = h*k + T;
		return h;
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null ) { // || !(obj instanceof DurativeAction<?, ?>) ) {
			return false;
		}
		@SuppressWarnings( "unchecked" )
		final DurativeAction<S, A> that = (DurativeAction<S, A>) obj;
		return T == that.T && pi.equals( that.pi );
	}
}
