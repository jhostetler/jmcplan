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
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.domains.cards.Card;

/**
 * @author jhostetler
 *
 */
public class BlackjackStateToken extends FactoredRepresentation<BlackjackState>
{
	private final int hash_code_;
	private final String repr_;
	
	private final float[] phi_;

	public BlackjackStateToken( final BlackjackState s )
	{
		assert( s.nplayers() == 1 );
		final HashCodeBuilder h = new HashCodeBuilder();
		final StringBuilder sb = new StringBuilder();
		h.append( s.dealerHand() );
		phi_ = new float[s.nplayers()*52 + 52];
		phi_[s.nplayers()*52 + s.dealerHand().get( 0 ).ordinal()] = 1;
		sb.append( "d:" ).append( s.dealerHand().toString() );
		for( int i = 0; i < s.nplayers(); ++i ) {
			sb.append( ", " ).append( i ).append( ":" );
			h.append( s.hand( i ) );
			for( final Card c : s.hand( i ) ) {
				phi_[52*i + c.ordinal()] += 1;
			}
			sb.append( s.hand( i ).toString() );
			h.append( s.passed( i ) );
			if( s.passed( i ) ) {
				sb.append( " passed" );
			}
		}
		hash_code_ = h.toHashCode();
		repr_ = sb.toString();
	}
	
	private BlackjackStateToken( final BlackjackStateToken that )
	{
		hash_code_ = that.hash_code_;
		repr_ = that.repr_;
		phi_ = that.phi_;
	}

	@Override
	public int hashCode()
	{
		return hash_code_;
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof BlackjackStateToken) ) {
			return false;
		}
		final BlackjackStateToken that = (BlackjackStateToken) obj;
		return repr_.equals( that.repr_ );
	}
	
	@Override
	public String toString()
	{
		return repr_;
	}

	@Override
	public BlackjackStateToken copy()
	{
		return new BlackjackStateToken( this );
	}

	@Override
	public float[] phi()
	{
		return phi_;
	}
}
