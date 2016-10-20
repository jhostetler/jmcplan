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
package edu.oregonstate.eecs.mcplan.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.util.Countdown;

/**
 * @author jhostetler
 *
 */
public class TimeLimitMctsNegamaxVisitor<S, A> implements MctsNegamaxVisitor<S, A>
{
	private static final Logger log = LoggerFactory.getLogger( TimeLimitMctsNegamaxVisitor.class );
	
	private final MctsNegamaxVisitor<S, A> inner_;
	private final Countdown countdown_;
	private long start_time_ = 0L;
	
	public TimeLimitMctsNegamaxVisitor( final MctsNegamaxVisitor<S, A> inner, final Countdown countdown )
	{
		inner_ = inner;
		countdown_ = countdown;
	}
	
	@Override
	public void startEpisode( final S s )
	{
		inner_.startEpisode( s );
		start_time_ = System.currentTimeMillis();
	}

	@Override
	public boolean startRollout( final S s )
	{
		final boolean inner_result = inner_.startRollout( s );
		final long now = System.currentTimeMillis();
		countdown_.count( now - start_time_ );
		start_time_ = now;
		if( countdown_.expired() ) {
			log.debug( "*** Time limit" );
			return false;
		}
		else {
			return inner_result;
		}
	}

	@Override
	public void startTree( final S s )
	{
		inner_.startTree( s );
	}

	@Override
	public void treeAction( final A a, final S sprime )
	{
		inner_.treeAction( a, sprime );
	}

	@Override
	public void treeDepthLimit( final S s )
	{
		inner_.treeDepthLimit( s );
	}

	@Override
	public void startDefault( final S s )
	{
		inner_.startDefault( s );
	}

	@Override
	public void defaultAction( final A a, final S sprime )
	{
		inner_.defaultAction( a, sprime );
	}

	@Override
	public void defaultDepthLimit( final S s )
	{
		inner_.defaultDepthLimit( s );
	}

	@Override
	public void depthLimit( final S s )
	{
		inner_.depthLimit( s );
	}

	@Override
	public double terminal( final S s )
	{
		return inner_.terminal( s );
	}

	@Override
	public boolean isTerminal( final S s )
	{
		return inner_.isTerminal( s );
	}
}
