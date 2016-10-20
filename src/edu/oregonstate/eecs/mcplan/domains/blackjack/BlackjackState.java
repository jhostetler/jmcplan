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

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.domains.cards.Card;
import edu.oregonstate.eecs.mcplan.domains.cards.Deck;

/**
 * @author jhostetler
 *
 */
public class BlackjackState implements State
{
	private final Deck deck_;
	
	private final ArrayList<Card> dealer_hand_ = new ArrayList<Card>();
	
	private final int nplayers_;
	
	private final boolean[] passed_;
	
	private final ArrayList<ArrayList<Card>> hands_;
	
	private final BlackjackParameters params_;
	
	public BlackjackState( final Deck deck, final int nplayers, final BlackjackParameters params )
	{
		deck_ = deck;
		nplayers_ = nplayers;
		params_ = params;
		dealer_hand_.add( deck_.deal() ); // Upcard
		passed_ = new boolean[nplayers_];
		hands_ = new ArrayList<ArrayList<Card>>( nplayers_ );
		for( int i = 0; i < nplayers_; ++i ) {
			final ArrayList<Card> h = new ArrayList<Card>();
			h.add( deck_.deal() );
			h.add( deck_.deal() );
			hands_.add( h );
		}
	}
	
	public BlackjackParameters parameters()
	{
		return params_;
	}
	
	public Deck deck()
	{
		return deck_;
	}
	
	public int nplayers()
	{
		return nplayers_;
	}
	
	public ArrayList<Card> hand( final int i )
	{
		return hands_.get( i );
	}
	
	public boolean passed( final int i )
	{
		return passed_[i];
	}
	
	public void setPassed( final int i, final boolean passed )
	{
		passed_[i] = passed;
	}
	
	public Card dealerUpcard()
	{
		return dealer_hand_.get( 0 );
	}
	
	public ArrayList<Card> dealerHand()
	{
		return dealer_hand_;
	}

//	@Override
//	public BlackjackStateToken token()
//	{
//		return new BlackjackStateToken( this );
//	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "d: " ).append( dealer_hand_ );
		for( int i = 0; i < hands_.size(); ++i ) {
			sb.append( ", " ).append( i ).append( ": " ).append( hands_.get( i ) );
		}
		return sb.toString();
//		return "STRING NOT IMPLEMENTED!"; //token().toString();
	}

	@Override
	public boolean isTerminal()
	{
		for( final boolean b : passed_ ) {
			if( !b ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void close()
	{ }
}
