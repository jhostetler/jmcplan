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
