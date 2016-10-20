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

import edu.oregonstate.eecs.mcplan.IndexedStateSpace;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.abstraction.ClusterAbstraction;

/**
 * @author jhostetler
 *
 */
public class TrivialClusterRepresenter implements Representer<BlackjackState, ClusterAbstraction<BlackjackState>>
{
	private final BlackjackParameters params_;
	private final IndexedStateSpace<BlackjackMdpState> ss_;
	
	public TrivialClusterRepresenter( final BlackjackParameters params, final IndexedStateSpace<BlackjackMdpState> ss )
	{
		params_ = params;
		ss_ = ss;
	}
	
	@Override
	public Representer<BlackjackState, ClusterAbstraction<BlackjackState>> create()
	{
		return new TrivialClusterRepresenter( params_, ss_ );
	}

	@Override
	public ClusterAbstraction<BlackjackState> encode( final BlackjackState s )
	{
		final int[] dv = params_.handValue( s.dealerHand() );
		final int[] pv = params_.handValue( s.hand( 0 ) );
		final BlackjackMdpState bj = new BlackjackMdpState( dv[0], dv[1],
															pv[0], pv[1], s.passed( 0 ) );
		return new ClusterAbstraction<BlackjackState>( ss_.id( bj ) );
	}

}
