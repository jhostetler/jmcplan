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
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;

/**
 * @author jhostetler
 *
 */
public class PrimitiveYahtzeeState extends FactoredRepresentation<YahtzeeState>
{
	private final float[] phi_;
	
	public PrimitiveYahtzeeState( final YahtzeeState s )
	{
		// Need: Hand, rerolls available, filled + score for each category,
		// Yahtzee & Upper bonuses
		final int Ncats = YahtzeeScores.values().length;
		phi_ = new float[Hand.Nfaces + 1 + (2 * Ncats) + 2];
		int idx = 0;
		final Hand h = s.hand();
		for( int i = 0; i < Hand.Nfaces; ++i ) {
			phi_[idx++] = h.dice[i];
		}
		phi_[idx++] = s.rerolls;
		for( final YahtzeeScores category : YahtzeeScores.values() ) {
			phi_[idx] = (s.filled[category.ordinal()] ? 1 : 0);
			phi_[idx + Ncats] = s.scores[category.ordinal()];
			++idx;
		}
		idx += Ncats;
		phi_[idx++] = s.yahtzee_bonus;
		phi_[idx++] = s.upper_bonus;
		assert( idx == phi_.length );
	}
	
	private PrimitiveYahtzeeState( final float[] phi )
	{
		phi_ = phi;
	}
	
	@Override
	public float[] phi()
	{
		return phi_;
	}

	@Override
	public PrimitiveYahtzeeState copy()
	{
		return new PrimitiveYahtzeeState( phi_ );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof PrimitiveYahtzeeState) ) {
			return false;
		}
		final PrimitiveYahtzeeState that = (PrimitiveYahtzeeState) obj;
		return Arrays.equals( phi_, that.phi_ );
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( phi_ );
	}
}
