/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.ActionSpace;
import edu.oregonstate.eecs.mcplan.MarkovDecisionProblem;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.StateSpace;
import edu.oregonstate.eecs.mcplan.dp.GreedyPolicy;
import edu.oregonstate.eecs.mcplan.dp.SparseValueIterationSolver;
import edu.oregonstate.eecs.mcplan.dp.ValueFunction;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class BlackjackMdp extends MarkovDecisionProblem<BlackjackMdpState, BlackjackAction>
{
	final int[] marginal_counts = new int[9];
	
	public BlackjackMdp( final StateSpace<BlackjackMdpState> ss,
			final ActionSpace<BlackjackMdpState, BlackjackAction> as )
	{
		super( ss, as );
		
		marginal_counts[0] = 4; // 4x2
		for( int i = 1; i < 8; ++i ) {
			marginal_counts[i] = marginal_counts[i - 1] + 4;
		}
		marginal_counts[8] = marginal_counts[7] + 16; // 16xT
	}

	@Override
	public double[] P( final BlackjackMdpState s, final BlackjackAction a )
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Pair<ArrayList<BlackjackMdpState>, ArrayList<Double>>
	sparseP( final BlackjackMdpState s, final BlackjackAction a )
	{
		final ArrayList<BlackjackMdpState> succ = new ArrayList<BlackjackMdpState>();
		final ArrayList<Double> p = new ArrayList<Double>();
		
		if( a instanceof PassAction ) {
			if( s == BlackjackMdpState.TheAbsorbingState ) {
				succ.add( BlackjackMdpState.TheAbsorbingState );
				p.add( 1.0 );
			}
			else if( !s.player_passed ) {
				succ.add( new BlackjackMdpState( s.dealer_value, s.dealer_high_aces,
												 s.player_value, s.player_high_aces, true ) );
				p.add( 1.0 );
			}
			else if( s.dealer_value > 16 ) {
				succ.add( BlackjackMdpState.TheAbsorbingState );
				p.add( 1.0 );
			}
			else {
				assert( s.player_passed );
				// Next card
				int nbust = 0;
				for( int i = 2; i <= 11; ++i ) {
					int dv = s.dealer_value + i;
					int da = s.dealer_high_aces;
					if( i == 11 ) {
						da += 1;
					}
					while( dv > 21 && da > 0 ) {
						dv -= 10;
						da -= 1;
					}
					if( dv > 21 ) {
						nbust += (i == 10) ? 16 : 4;
					}
					else {
						succ.add( new BlackjackMdpState( dv, da,
														 s.player_value, s.player_high_aces, true ) );
						p.add( (i == 10 ? 16 : 4) / 52.0 );
					}
				}
				if( nbust > 0 ) {
					succ.add( new BlackjackMdpState( 22, 0,
													 s.player_value, s.player_high_aces, true ) );
					p.add( nbust / 52.0 );
				}
			}
		}
		else { // HitAction
			assert( !s.player_passed );
			assert( !(s.player_value > 21) );
			assert( !(s.dealer_value > 11) ); // Dealer hasn't acted yet
			assert( s != BlackjackMdpState.TheAbsorbingState );
			// Next player card
			int nbust = 0;
			for( int i = 2; i <= 11; ++i ) {
				int pv = s.player_value + i;
				int pa = s.player_high_aces;
				if( i == 11 ) {
					pa += 1;
				}
				while( pv > 21 && pa > 0 ) {
					pv -= 10;
					pa -= 1;
				}
				if( pv > 21 ) {
					nbust += (i == 10) ? 16 : 4;
				}
				else {
					succ.add( new BlackjackMdpState( s.dealer_value, s.dealer_high_aces,
													 pv, pa, false ) );
					p.add( (i == 10 ? 16 : 4) / 52.0 );
				}
			}
			if( nbust > 0 ) {
				succ.add( new BlackjackMdpState( s.dealer_value, s.dealer_high_aces,
												 22, 0, true ) );
				p.add( nbust / 52.0 );
			}
		}
		
		return Pair.makePair( succ, p );
	}

	@Override
	public double P( final BlackjackMdpState s, final BlackjackAction a, final BlackjackMdpState sprime )
	{
		return 0.0;
	}

	@Override
	public double R( final BlackjackMdpState s, final BlackjackAction a )
	{
		if( !s.player_passed || s.dealer_value <= 16 ) {
			return 0.0;
		}
		else if( s == BlackjackMdpState.TheAbsorbingState ) {
			return 0.0;
		}
		
		final int pv = s.player_value;
		final int dv = s.dealer_value;
		if( pv > 21 ) {
			return -1.0;
		}
		else if( dv > 21 ) {
			return 1.0;
		}
		else {
			if( pv > dv ) {
				return 1.0;
			}
			else if( pv < dv ) {
				return -1.0;
			}
			else {
				return 0.0;
			}
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv )
	{
		final BlackjackMdp mdp = new BlackjackMdp( new BlackjackStateSpace(), new BlackjackActionSpace() );
		
//		for( int v = 2; v <= 21; ++v ) {
//			System.out.println( "v = " + v );
//			final BlackjackMdpState s = new BlackjackMdpState( v, 0, 21, 0, true );
//			final BlackjackAction a = new PassAction( 0 );
//			final Pair<ArrayList<BlackjackMdpState>, ArrayList<Double>> P = mdp.sparseP( s, a );
//			double sum = 0.0;
//			for( int i = 0; i < P.first.size(); ++i ) {
//				System.out.print( P.second.get( i ) );
//				System.out.print( ": " );
//				System.out.println( P.first.get( i ).toString() );
//				sum += P.second.get( i );
//			}
//			System.out.println( "***** P = " + sum );
//			System.out.println( "***** R = " + mdp.R( s, a ) );
//		}
		
		final SparseValueIterationSolver<BlackjackMdpState, BlackjackAction> vi
			= new SparseValueIterationSolver<BlackjackMdpState, BlackjackAction>( mdp );
		vi.run();
		final ValueFunction<BlackjackMdpState> Vstar = vi.Vstar();
		
		final Policy<BlackjackMdpState, BlackjackAction> pi
			= new GreedyPolicy<BlackjackMdpState, BlackjackAction>( mdp, Vstar );
		
		final String[][] hard_actions = new String[18][10];
		final String[][] soft_actions = new String[10][10];
		final Generator<BlackjackMdpState> S = mdp.S().generator();
		while( S.hasNext() ) {
			final BlackjackMdpState s = S.next();
			pi.setState( s, 0L );
			final BlackjackAction a = pi.getAction();
			System.out.println( s.toString() + ": " + a );
			final String short_string = (a instanceof PassAction ? "S" : "H");
			if( !s.player_passed && s.player_high_aces == 0 ) {
				hard_actions[s.player_value - 4][s.dealer_value - 2] = short_string;
			}
			else if( !s.player_passed && s.player_high_aces > 0 ) {
				soft_actions[s.player_value - 12][s.dealer_value - 2] = short_string;
			}
		}
		System.out.println( "Optimal strategy for hard player hand:" );
		System.out.println( "\t2 3 4 5 6 7 8 9 T A" );
		System.out.println( "\t-------------------" );
//		for( int i = 17; i >= 0; --i ) {
		for( int i = 0; i < 18; ++i ) {
			System.out.print( i + 4 );
			System.out.print( "\t" );
			for( int j = 0; j < 10; ++j ) {
				System.out.print( hard_actions[i][j] );
				System.out.print( " " );
			}
			System.out.println();
		}
		System.out.println();
		System.out.println( "Optimal strategy for soft player hand:" );
		System.out.println( "\t2 3 4 5 6 7 8 9 T A" );
		System.out.println( "\t-------------------" );
//		for( int i = 8; i >= 0; --i ) {
		for( int i = 0; i < 10; ++i ) {
			System.out.print( i + 12 );
			System.out.print( "\t" );
			for( int j = 0; j < 10; ++j ) {
				System.out.print( soft_actions[i][j] );
				System.out.print( " " );
			}
			System.out.println();
		}
		
	}

}
