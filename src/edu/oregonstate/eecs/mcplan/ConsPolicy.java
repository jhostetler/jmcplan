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


/**
 * A Policy that executes a particular action and then follows a specified
 * Policy afterwards.
 *
 * @param <S>
 * @param <A>
 */
public final class ConsPolicy<S, A extends VirtualConstructor<A>> extends AnytimePolicy<S, A>
{
	private final A a0;
	private final Policy<S, A> pi;
	private final Policy<S, A> pi_template;
	/**
	 * 0: reset and no state set; 1: reset and first state set, will return
	 * a0 for the action; 2: following pi.
	 */
	private int nonstationary_step = 0;
	
	public ConsPolicy( final A a0, final Policy<S, A> pi )
	{
		this.a0 = a0;
		this.pi_template = pi;
		this.pi = this.pi_template.copy();
	}
	
	private ConsPolicy( final ConsPolicy<S, A> that )
	{
		this.a0 = that.a0;
		this.pi_template = that.pi_template;
		this.pi = that.pi.copy();
		this.nonstationary_step = that.nonstationary_step;
	}
	
	@Override
	public ConsPolicy<S, A> copy()
	{
		return new ConsPolicy<S, A>( this );
	}
	
	@Override
	public void setState( final S s, final long t )
	{
//		if( nonstationary_step > 0 ) {
			pi.setState( s, t );
//		}
		if( nonstationary_step < 2 ) {
			nonstationary_step += 1;
		}
	}

	@Override
	public A getAction()
	{
		if( nonstationary_step == 1 ) {
			return a0.create();
		}
		else {
			return pi.getAction();
		}
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{ return getClass().toString(); }

	@Override
	public int hashCode()
	{ return System.identityHashCode( this ); }

	@Override
	public boolean equals( final Object that )
	{ return this == that; }
	
	@Override
	public String toString()
	{
		return "ConsPolicy(" + a0 + "; " + pi_template + "; nss: " + nonstationary_step + ")";
	}

	@Override
	public boolean improvePolicy()
	{
		return false;
	}
}
