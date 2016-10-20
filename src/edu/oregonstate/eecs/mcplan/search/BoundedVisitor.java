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

package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.util.Countdown;

/**
 * A ForwardingNegamaxVisitor that tracks total execution time and stops the
 * search when time exceeds a limit. Time is measured on each call to
 * discoverVertex(), so actual time used may be quite a bit longer than
 * requested.
 * 
 * @author jhostetler
 *
 * @param <S>
 * @param <A>
 */
public class BoundedVisitor<S, A> extends ForwardingNegamaxVisitor<S, A>
{
	public final Countdown countdown_;
	private long start_time_ = 0L;
	
	public PrincipalVariation<S, A> pv_ = null;
	
	public BoundedVisitor( final NegamaxVisitor<S, A> inner, final Countdown countdown )
	{
		super( inner );
		countdown_ = countdown;
	}
	
	@Override
	public void startVertex( final S s )
	{
		super.startVertex( s );
		start_time_ = System.currentTimeMillis();
	}
	
	@Override
	public boolean discoverVertex( final S s )
	{
		final boolean inner_result = super.discoverVertex( s );
		final long now = System.currentTimeMillis();
		countdown_.count( now - start_time_ );
		start_time_ = now;
		if( countdown_.expired() ) {
			System.out.println( "*** Time limit" );
			return true;
		}
		else {
			return inner_result;
	
		}
	}
	
	@Override
	public void principalVariation( final PrincipalVariation<S, A> pv )
	{
		pv_ = pv;
		super.principalVariation( pv );
	}
}