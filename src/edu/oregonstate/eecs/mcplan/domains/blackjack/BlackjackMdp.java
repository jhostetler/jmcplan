/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.MarkovDecisionProblem;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.domains.cards.Deck;
import edu.oregonstate.eecs.mcplan.domains.cards.InfiniteDeck;
import edu.oregonstate.eecs.mcplan.dp.GreedyPolicy;
import edu.oregonstate.eecs.mcplan.dp.SparseValueIterationSolver;
import edu.oregonstate.eecs.mcplan.dp.ValueFunction;
import edu.oregonstate.eecs.mcplan.sim.Episode;
import edu.oregonstate.eecs.mcplan.util.Generator;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class BlackjackMdp extends MarkovDecisionProblem<BlackjackMdpState, BlackjackAction>
{
//	private final int[] marginal_counts = new int[9];
	
	private final BlackjackParameters params_;
	private final BlackjackStateSpace ss_;
	private final BlackjackActionSpace as_;
	
	public BlackjackMdp( final BlackjackParameters params )
	{
		params_ = params;
		ss_ = new BlackjackStateSpace( params );
		as_ = new BlackjackActionSpace( params );
		
//		marginal_counts[0] = 4; // 4x2
//		for( int i = 1; i < 8; ++i ) {
//			marginal_counts[i] = marginal_counts[i - 1] + 4;
//		}
//		marginal_counts[8] = marginal_counts[7] + 16; // 16xT
	}
	
	@Override
	public BlackjackStateSpace S()
	{
		return ss_;
	}

	@Override
	public BlackjackActionSpace A()
	{
		return as_;
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
			else if( s.dealer_value > params_.dealer_threshold ) {
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
					while( dv > params_.max_score && da > 0 ) {
						dv -= 10;
						da -= 1;
					}
					if( dv > params_.max_score ) {
						nbust += (i == 10) ? 16 : 4;
					}
					else {
						succ.add( new BlackjackMdpState( dv, da,
														 s.player_value, s.player_high_aces, true ) );
						p.add( (i == 10 ? 16 : 4) / 52.0 );
					}
				}
				if( nbust > 0 ) {
					succ.add( new BlackjackMdpState( params_.busted_score, 0,
													 s.player_value, s.player_high_aces, true ) );
					p.add( nbust / 52.0 );
				}
			}
		}
		else { // HitAction
			assert( !s.player_passed );
			assert( !(s.player_value > params_.max_score) );
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
				while( pv > params_.max_score && pa > 0 ) {
					pv -= 10;
					pa -= 1;
				}
				if( pv > params_.max_score ) {
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
												 params_.busted_score, 0, true ) );
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
		if( !s.player_passed || s.dealer_value <= params_.dealer_threshold ) {
			return 0.0;
		}
		else if( s == BlackjackMdpState.TheAbsorbingState ) {
			return 0.0;
		}
		
		final int pv = s.player_value;
		final int dv = s.dealer_value;
		if( pv > params_.max_score ) {
			return -1.0;
		}
		else if( dv > params_.max_score ) {
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
	
	@Override
	public double R( final BlackjackMdpState s )
	{
		return 0;
	}
	
	public Pair<String[][], String[][]> solve()
	{
		final SparseValueIterationSolver<BlackjackMdpState, BlackjackAction> vi
			= new SparseValueIterationSolver<BlackjackMdpState, BlackjackAction>( this );
		vi.run();
		final ValueFunction<BlackjackMdpState> Vstar = vi.Vstar();
		
		final Policy<BlackjackMdpState, BlackjackAction> pi
			= new GreedyPolicy<BlackjackMdpState, BlackjackAction>( this, Vstar );
		
		final String[][] hard_actions = new String[params_.hard_hand_count][params_.dealer_showing_count];
		final String[][] soft_actions = new String[params_.soft_hand_count][params_.dealer_showing_count];
		final Generator<BlackjackMdpState> S = this.S().generator();
		while( S.hasNext() ) {
			final BlackjackMdpState s = S.next();
			pi.setState( s, 0L );
			final BlackjackAction a = pi.getAction();
//			System.out.println( s.toString() + ": " + a );
			final String short_string = (a instanceof PassAction ? "S" : "H");
			if( !s.player_passed && s.player_high_aces == 0 ) {
				hard_actions[s.player_value - params_.hard_hand_min]
							[s.dealer_value - params_.dealer_showing_min] = short_string;
			}
			else if( !s.player_passed && s.player_high_aces > 0 ) {
				soft_actions[s.player_value - params_.soft_hand_min]
							[s.dealer_value - params_.dealer_showing_min] = short_string;
			}
		}
		System.out.println( "Optimal strategy for hard player hand:" );
		System.out.println( "\t2 3 4 5 6 7 8 9 T A" );
		System.out.println( "\t-------------------" );
//		for( int i = 17; i >= 0; --i ) {
		for( int i = 0; i < params_.hard_hand_count; ++i ) {
			System.out.print( i + params_.hard_hand_min );
			System.out.print( "\t" );
			for( int j = 0; j < params_.dealer_showing_count; ++j ) {
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
		for( int i = 0; i < params_.soft_hand_count; ++i ) {
			System.out.print( i + params_.soft_hand_min );
			System.out.print( "\t" );
			for( int j = 0; j < params_.dealer_showing_count; ++j ) {
				System.out.print( soft_actions[i][j] );
				System.out.print( " " );
			}
			System.out.println();
		}
		
		return Pair.makePair( hard_actions, soft_actions );
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv )
	{
		final BlackjackParameters params = new BlackjackParameters();
		final BlackjackMdp mdp = new BlackjackMdp( params );
		
//		for( int v = 2; v <= max_score; ++v ) {
//			System.out.println( "v = " + v );
//			final BlackjackMdpState s = new BlackjackMdpState( v, 0, max_score, 0, true );
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
		
		final Policy<BlackjackState, JointAction<BlackjackAction>> pi_mod
			= new Policy<BlackjackState, JointAction<BlackjackAction>>() {

				BlackjackMdpState s_ = null;
			
				@Override
				public void setState( final BlackjackState s, final long t )
				{
					final int[] dv = params.handValue( s.dealerHand() );
					final int[] pv = params.handValue( s.hand( 0 ) );
					final boolean pp = s.passed( 0 );
					s_ = new BlackjackMdpState( dv[0], dv[1], pv[0], pv[1], pp );
					pi.setState( s_, 0L );
				}

				@Override
				public JointAction<BlackjackAction> getAction()
				{ return new JointAction<BlackjackAction>( pi.getAction().create() ); }

				@Override
				public void actionResult( final BlackjackState sprime, final double[] r )
				{ }

				@Override
				public String getName()
				{ return "blackjack-optimal"; }

				@Override
				public int hashCode()
				{ return pi.hashCode(); }

				@Override
				public boolean equals( final Object that )
				{ return this == that; }
		};
		
		
		final String[][] hard_actions = new String[params.hard_hand_count][params.dealer_showing_count];
		final String[][] soft_actions = new String[params.soft_hand_count][params.dealer_showing_count];
		final Generator<BlackjackMdpState> S = mdp.S().generator();
		while( S.hasNext() ) {
			final BlackjackMdpState s = S.next();
			pi.setState( s, 0L );
			final BlackjackAction a = pi.getAction();
			System.out.println( s.toString() + ": " + a );
			final String short_string = (a instanceof PassAction ? "S" : "H");
			if( !s.player_passed && s.player_high_aces == 0 ) {
				hard_actions[s.player_value - params.hard_hand_min]
							[s.dealer_value - params.dealer_showing_min] = short_string;
			}
			else if( !s.player_passed && s.player_high_aces > 0 ) {
				soft_actions[s.player_value - params.soft_hand_min]
							[s.dealer_value - params.dealer_showing_min] = short_string;
			}
		}
		System.out.println( "Optimal strategy for hard player hand:" );
		System.out.println( "\t2 3 4 5 6 7 8 9 T A" );
		System.out.println( "\t-------------------" );
//		for( int i = 17; i >= 0; --i ) {
		for( int i = 0; i < params.hard_hand_count; ++i ) {
			System.out.print( i + params.hard_hand_min );
			System.out.print( "\t" );
			for( int j = 0; j < params.dealer_showing_count; ++j ) {
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
		for( int i = 0; i < params.soft_hand_count; ++i ) {
			System.out.print( i + params.soft_hand_min );
			System.out.print( "\t" );
			for( int j = 0; j < params.dealer_showing_count; ++j ) {
				System.out.print( soft_actions[i][j] );
				System.out.print( " " );
			}
			System.out.println();
		}
		
		
		final int Ngames = 100000;
		final MeanVarianceAccumulator ret = new MeanVarianceAccumulator();
		for( int i = 0; i < Ngames; ++i ) {
			if( i % 100000 == 0 ) {
				System.out.println( "Episode " + i );
			}

			final Deck deck = new InfiniteDeck();
			final BlackjackState s0 = new BlackjackState( deck, 1, params );
			final BlackjackSimulator sim = new BlackjackSimulator( s0 );

			final Episode<BlackjackState, BlackjackAction> episode
				= new Episode<BlackjackState, BlackjackAction>(	sim, pi_mod );
			episode.run();
			ret.add( sim.reward()[0] );
		}
		System.out.println( "****************************************" );
		System.out.println( "Average return: " + ret.mean() );
		System.out.println( "Return variance: " + ret.variance() );
		final double conf = 0.975 * ret.variance() / Math.sqrt( Ngames );
		System.out.println( "Confidence: " + conf );
		System.out.println();
//		data_out.print( repr );
//		data_out.print( "," + Nepisodes );
//		data_out.print( "," + ret.mean() );
//		data_out.print( "," + ret.variance() );
//		data_out.print( "," + conf );
//		data_out.println();
	}

}
