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
import edu.oregonstate.eecs.mcplan.util.Generator;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;

/**
 * @author jhostetler
 *
 */
public class BlackjackStateSpace extends IndexedStateSpace<BlackjackMdpState>
{
	private final ArrayList<BlackjackMdpState> states_ = new ArrayList<BlackjackMdpState>();
	
	public BlackjackStateSpace( final BlackjackParameters params )
	{
//		for( int dv = 2; dv <= 11; ++dv ) {
//			// Dealer hasn't acted yet
//			for( int pv = 2; pv <= 22; ++pv ) {
//				if( pv <= 21 ) {
//					states_.add( new BlackjackMdpState( dv, 0, pv, 0, false ) );
//				}
//				states_.add( new BlackjackMdpState( dv, 0, pv, 0, true ) );
//				if( pv >= 11 && pv <= 21 ) {
//					// Player can have high ace, but it's impossible to bust
//					// with a high ace
//					states_.add( new BlackjackMdpState( dv, 0, pv, 1, false ) );
//					states_.add( new BlackjackMdpState( dv, 0, pv, 1, true ) );
//				}
//			}
//		}
//		for( int dv = 12; dv <= 22; ++dv ) {
//			// Dealer must have acted -> player has passed
//			for( int pv = 2; pv <= 22; ++pv ) {
//				states_.add( new BlackjackMdpState( dv, 0, pv, 0, true ) );
//				if( pv >= 11 && pv <= 21 ) {
//					// Player can have high ace, but it's impossible to bust
//					// with a high ace
//					states_.add( new BlackjackMdpState( dv, 0, pv, 1, true ) );
//				}
//			}
//		}
		
//		for( int dv = params.dealer_showing_min; dv <= params.busted_score; ++dv ) {
//			// Dealer doesn't have high ace
//			for( int pv = params.hard_hand_min; pv <= params.busted_score; ++pv ) {
//				if( pv <= params.max_score && dv <= 11 ) {
//					states_.add( new BlackjackMdpState( dv, 0, pv, 0, false ) );
//				}
//				states_.add( new BlackjackMdpState( dv, 0, pv, 0, true ) );
//				if( pv >= params.soft_hand_min && pv <= params.max_score ) {
//					// Player can have high ace, but it's impossible to bust
//					// with a high ace
//					if( dv <= 11 ) {
//						states_.add( new BlackjackMdpState( dv, 0, pv, 1, false ) );
//					}
//					states_.add( new BlackjackMdpState( dv, 0, pv, 1, true ) );
//				}
//			}
//			if( dv >= 11 && dv <= params.max_score ) {
//				// Dealer can have high ace, but will never bust with high ace
//				for( int pv = params.hard_hand_min; pv <= params.busted_score; ++pv ) {
//					if( pv <= params.max_score && dv <= 11 ) {
//						states_.add( new BlackjackMdpState( dv, 1, pv, 0, false ) );
//					}
//					states_.add( new BlackjackMdpState( dv, 1, pv, 0, true ) );
//					if( pv >= params.soft_hand_min && pv <= params.max_score ) {
//						// Player can have high ace, but it's impossible to bust
//						// with a high ace
//						if( dv <= 11 ) {
//							states_.add( new BlackjackMdpState( dv, 1, pv, 1, false ) );
//						}
//						states_.add( new BlackjackMdpState( dv, 1, pv, 1, true ) );
//					}
//				}
//			}
//		}
		
		for( int dv = params.dealer_showing_min; dv <= params.busted_score; ++dv ) {
			// Dealer doesn't have high ace
			for( int pv = params.hard_hand_min; pv <= params.busted_score; ++pv ) {
				final int da = dv / 11;
				for( int nda = 0; nda <= da; ++nda ) {
					if( pv <= params.max_score && dv <= 11 ) {
						states_.add( new BlackjackMdpState( dv, nda, pv, 0, false ) );
					}
					states_.add( new BlackjackMdpState( dv, nda, pv, 0, true ) );
					if( pv >= params.soft_hand_min && pv <= params.max_score ) {
						final int pa = pv / 11; // Max # high aces player could have
						for( int na = 0; na <= pa; ++na ) {
							// Player can have high ace, but it's impossible to bust
							// with a high ace
							if( dv <= 11 ) {
								states_.add( new BlackjackMdpState( dv, nda, pv, na, false ) );
							}
							states_.add( new BlackjackMdpState( dv, nda, pv, na, true ) );
						}
					}
				}
			}
			
		}
		
		// Absorbing state
		states_.add( BlackjackMdpState.TheAbsorbingState );
		
		int id = 0;
		for( final BlackjackMdpState s : states_ ) {
			id_map_.put( s, id++ );
		}
	}
	
	private final TObjectIntMap<BlackjackMdpState> id_map_ = new TObjectIntHashMap<BlackjackMdpState>();
	
	@Override
	public int id( final BlackjackMdpState s )
	{
		return id_map_.get( s );
	}
	
	@Override
	public int cardinality()
	{
		return states_.size();
	}

	@Override
	public boolean isFinite()
	{
		return true;
	}

	@Override
	public boolean isCountable()
	{
		return true;
	}

	@Override
	public Generator<BlackjackMdpState> generator()
	{
		return Generator.fromIterator( states_.iterator() );
	}

}
