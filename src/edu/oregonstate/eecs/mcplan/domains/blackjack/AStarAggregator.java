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

import edu.oregonstate.eecs.mcplan.Representer;

/**
 * @deprecated
 * @author jhostetler
 *
 */
@Deprecated
public class AStarAggregator implements Representer<BlackjackState, AStarAbstraction>
{
	// Optimal strategy computed with value iteration
	private final String[][] hard_actions_ =
		{	{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "S", "S", "S", "H", "H", "H", "H", "H"},
			{"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},
			{"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},
			{"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},
			{"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},
			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},
			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},
			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},
			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},
			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"}	};
	private final String[][] soft_actions_ =
		{	{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"S", "S", "S", "S", "S", "S", "S", "H", "H", "H"},
			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},
			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},
			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"}	};
	
//	public AStarAggregator( final String[][] hard_actions, final String[][] soft_actions )
//	{
//		hard_actions_ = hard_actions;
//		soft_actions_ = soft_actions;
//	}
	
	private final BlackjackParameters params_;
	
	public AStarAggregator( final BlackjackParameters params )
	{
		params_ = params;
	}

	@Override
	public Representer<BlackjackState, AStarAbstraction> create()
	{
		return new AStarAggregator( params_ );
	}

	@Override
	public AStarAbstraction encode( final BlackjackState s )
	{
		final int[] pv = params_.handValue( s.hand( 0 ) );
		final int dv = s.dealerUpcard().BlackjackValue();
//		System.out.println( s.hand( 0 ).toString() );
//		System.out.println( "pv: " + pv[0] + ", dv: " + dv );
		final String as;
		if( !s.passed( 0 ) ) {
			if( pv[1] == 0 ) {
				as = hard_actions_[pv[0] - params_.hard_hand_min][dv - params_.dealer_showing_min];
			}
			else { // pv[1] > 0
				as = soft_actions_[pv[0] - params_.soft_hand_min][dv - params_.dealer_showing_min];
			}
		}
		else {
			as = "T";
		}
		
		if( "S".equals( as ) ) {
			return new AStarAbstraction( new PassAction( 0 ), dv, pv[0] );
		}
		else if( "H".equals( as ) ) {
			return new AStarAbstraction( new HitAction( 0 ), dv, pv[0] );
		}
		else {
			return new AStarAbstraction( null, dv, pv[0] );
		}
	}
	
	@Override
	public String toString()
	{
		return "chi-inf";
	}
}
