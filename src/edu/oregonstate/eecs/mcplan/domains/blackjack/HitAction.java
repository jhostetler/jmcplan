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

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.domains.cards.Card;

/**
 * @author jhostetler
 *
 */
public class HitAction extends BlackjackAction
{
	public final int player;
	
	private boolean done_ = false;
	private Card c_ = null;
	
	public HitAction( final int player )
	{
		this.player = player;
	}
	
	@Override
	public void undoAction( final BlackjackState s )
	{
		assert( done_ );
		s.deck().undeal( c_ );
		final ArrayList<Card> h = s.hand( player );
		h.remove( h.size() - 1 );
		s.setPassed( player, false ); // In case player busted
		done_ = false;
	}

	@Override
	public void doAction( final RandomGenerator rng, final BlackjackState s )
	{
		assert( !done_ );
		c_ = s.deck().deal();
		s.hand( player ).add( c_ );
		if( s.parameters().handValue( s.hand( player ) )[0] > s.parameters().max_score ) {
			s.setPassed( player, true );
		}
		done_ = true;
	}

	@Override
	public boolean isDone()
	{
		return done_;
	}

	@Override
	public BlackjackAction create()
	{
		return new HitAction( player );
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof HitAction) ) {
			return false;
		}
		final HitAction that = (HitAction) obj;
		return player == that.player;
	}
	
	@Override
	public int hashCode()
	{
		return 41 + player;
	}
	
	@Override
	public String toString()
	{
		return "HitAction[" + player + "]";
	}
}
