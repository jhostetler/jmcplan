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

import edu.oregonstate.eecs.mcplan.domains.cards.Card;
import edu.oregonstate.eecs.mcplan.domains.cards.Rank;

/**
 * @author jhostetler
 *
 */
public class BlackjackParameters
{
	public final int max_score = 32; //21; //32; //21; //32; //27; //32; // 42; // 35; // 27; // 23; // 27; // 21
	public final int busted_score = max_score + 1;
	public final int dealer_threshold = max_score - 5;
	
	public final int dealer_showing_count = 10;
	public final int dealer_showing_min = 2;
	
	public final int hard_hand_min = 4;
	public final int hard_hand_count = max_score - hard_hand_min + 1; // 18;
	
	public final int soft_hand_min = 12;
	public final int soft_hand_count = max_score - soft_hand_min + 1; // 10;
	
	public int[] handValue( final Iterable<Card> hand )
	{
		int total = 0;
		int high_aces = 0;
		for( final Card c : hand ) {
			total += c.BlackjackValue();
			if( c.rank == Rank.R_A ) {
				high_aces += 1;
			}
			while( total > max_score && high_aces > 0 ) {
				total -= 10;
				high_aces -= 1;
			}
		}
		return new int[] { total, high_aces };
	}
}
