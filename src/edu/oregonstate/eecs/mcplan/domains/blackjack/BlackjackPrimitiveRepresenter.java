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

package edu.oregonstate.eecs.mcplan.domains.blackjack;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.domains.cards.Card;

public class BlackjackPrimitiveRepresenter implements FactoredRepresenter<BlackjackState, FactoredRepresentation<BlackjackState>>
{
	private final int nagents_;
	private final ArrayList<Attribute> attributes_;
	
	public BlackjackPrimitiveRepresenter( final int nagents )
	{
		nagents_ = nagents;
		attributes_ = new ArrayList<Attribute>();
		for( int p = 0; p < nagents_ + 1; ++p ) {
			final String player = (p < nagents_ ? "p" + p : "d");
			for( int i = 0; i < 52; ++i ) {
				final Card c = Card.values()[i];
				attributes_.add( new Attribute( player + "_" + c ) );
			}
		}
		assert( attributes_.size() == (nagents_ + 1)*52 );
	}
	
	private BlackjackPrimitiveRepresenter( final BlackjackPrimitiveRepresenter that )
	{
		nagents_ = that.nagents_;
		attributes_ = that.attributes_;
	}
	
	@Override
	public BlackjackPrimitiveRepresenter create()
	{
		return new BlackjackPrimitiveRepresenter( this );
	}

	@Override
	public BlackjackStateToken encode( final BlackjackState s )
	{
		return new BlackjackStateToken( s );
	}
	
	@Override
	public String toString()
	{
		return "flat";
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes_;
	}
}