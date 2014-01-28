/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.StateSpace;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class BlackjackStateSpace extends StateSpace<BlackjackMdpState>
{
	private final ArrayList<BlackjackMdpState> states_ = new ArrayList<BlackjackMdpState>();
	
	public BlackjackStateSpace()
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
		
		for( int dv = 2; dv <= 22; ++dv ) {
			// Dealer doesn't have high ace
			for( int pv = 4; pv <= 22; ++pv ) {
				if( pv <= 21 && dv <= 11 ) {
					states_.add( new BlackjackMdpState( dv, 0, pv, 0, false ) );
				}
				states_.add( new BlackjackMdpState( dv, 0, pv, 0, true ) );
				if( pv >= 12 && pv <= 21 ) {
					// Player can have high ace, but it's impossible to bust
					// with a high ace
					if( dv <= 11 ) {
						states_.add( new BlackjackMdpState( dv, 0, pv, 1, false ) );
					}
					states_.add( new BlackjackMdpState( dv, 0, pv, 1, true ) );
				}
			}
			if( dv >= 11 && dv <= 21 ) {
				// Dealer can have high ace, but will never bust with high ace
				for( int pv = 4; pv <= 22; ++pv ) {
					if( pv <= 21 && dv <= 11 ) {
						states_.add( new BlackjackMdpState( dv, 1, pv, 0, false ) );
					}
					states_.add( new BlackjackMdpState( dv, 1, pv, 0, true ) );
					if( pv >= 12 && pv <= 21 ) {
						// Player can have high ace, but it's impossible to bust
						// with a high ace
						if( dv <= 11 ) {
							states_.add( new BlackjackMdpState( dv, 1, pv, 1, false ) );
						}
						states_.add( new BlackjackMdpState( dv, 1, pv, 1, true ) );
					}
				}
			}
		}
		
		// Absorbing state
		states_.add( BlackjackMdpState.TheAbsorbingState );
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
