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
package edu.oregonstate.eecs.mcplan.domains.spbj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.domains.cards.Card;
import edu.oregonstate.eecs.mcplan.domains.cards.Deck;
import edu.oregonstate.eecs.mcplan.domains.cards.StackedDeck;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * Simulator for "Spanish 21". Rules for Spanish 21 are somewhat variable. We
 * use the following variant rules:
 * 
 * - Dealer stands on soft 17 - this is not standard when re-doubling is allowed
 * - Dealer does not immediately reveal natural Blackjack - nonstandard
 * - Player does not immediately reveal natural Blackjack - nonstandard
 * - Player may double up to 3 times
 * - Player may double after splitting
 * - Player may re-split up to a total of 4 hands
 * 
 * The main alteration is that gameplay rules that cause the game to end
 * before any decisions are made have been removed. We use S17 merely because
 * it eliminates a special case in the code.
 * 
 * The nonstandard aspects of the rules should decrease the house advantage.
 * Specifically, being able to draw to beat the dealer's Blackjack favors
 * the player, and S17 is better for the player than H17 (usually if
 * re-doubling is allowed then H17 is the rule).
 */
public class SpBjSimulator implements UndoSimulator<SpBjState, SpBjAction>
{
	private final SpBjState s;
//	private final double r;
	
	private final Deque<JointAction<SpBjAction>> action_history
		= new ArrayDeque<JointAction<SpBjAction>>();
	
	public SpBjSimulator( final SpBjState s )
	{
		this.s = s;
//		r = 0;
	}
	
	@Override
	public SpBjState state()
	{
		return s;
	}

	@Override
	public void takeAction( final JointAction<SpBjAction> a )
	{
		for( final SpBjAction ai : a ) {
			ai.doAction( s );
		}
		action_history.push( a );
		
//		if( isTerminalState() ) {
//			completeDealerHand();
//		}
	}

	@Override
	public void untakeLastAction()
	{
//		if( isTerminalState() ) {
//			uncompleteDealerHand();
//		}
		
		final JointAction<SpBjAction> a = action_history.pop();
		for( final SpBjAction ai : a ) {
			ai.undoAction( s );
		}
	}

	@Override
	public long depth()
	{
		return action_history.size();
	}

	@Override
	public long t()
	{
		return action_history.size();
	}

	@Override
	public int nagents()
	{
		return 1;
	}

	@Override
	public int[] turn()
	{
		if( s.isTerminal() ) {
			return new int[] { 0 };
		}
		else {
			return new int[] { };
		}
	}
	
//	private void completeDealerHand()
//	{
//		final ArrayList<Card> hand = s.dealerHand();
//		int[] dv = null;
//		while( true ) {
//			if( hand.size() < 2 ) {
//				hand.add( s.deck().deal() );
//			}
//			else {
//				dv = SpBjHand.handValue( hand );
//				// Dealer stands on soft 17
//				if( dv[0] <= SpBjHand.dealer_threshold ) {
//					hand.add( s.deck().deal() );
//				}
//				else {
//					break;
//				}
//			}
//		}
//
//		final ArrayList<int[]> pv = s.player_hand.values();
//		assert( r == 0 );
//
//		if( s.player_hand.Nhands == 1
//				&& pv.get( 0 )[0] == SpBjHand.max_score
//				&& s.player_hand.hands.get( 0 ).size() == 2 ) {
//			// Natural Blackjack always wins and pays 3:2
//			r += 3*s.player_hand.bets[0] / 2;
//			return;
//		}
//
//		for( int i = 0; i < pv.size(); ++i ) {
//			final int[] pvi = pv.get( i );
//			// Player bust
//			if( pvi[0] > SpBjHand.max_score ) {
//				r += -s.player_hand.bets[i];
//			}
//			// Player has 21
//			else if( pvi[0] == SpBjHand.max_score ) {
//				// Player didn't double -> eligible for bonuses
//				if( s.player_hand.bets[i] == SpBjHand.min_bet ) {
//					final ArrayList<Card> h = s.player_hand.hands.get( i );
//					if( h.size() == 5 ) { // 5-card 21 pays 3:2
//						r += 3*s.player_hand.bets[i] / 2;
//						continue;
//					}
//					else if( h.size() == 6 ) { // 6-card 21 pays 2:1
//						r += 2*s.player_hand.bets[i];
//						continue;
//					}
//					else if( h.size() >= 7 ) { // 7-or-more-card 21 pays 3:1
//						r += 3*s.player_hand.bets[i];
//						continue;
//					}
//					else if( h.size() == 3 ) {
//						// Check for 678 / 777 bonus
//						final ArrayList<Card> sorted = Fn.copy( h );
//						Collections.sort( sorted, Card.TheAceHighRankComparator );
//						final Card c1 = sorted.get( 1 );
//						if( c1.rank == Rank.R_7 ) {
//							final Card c0 = sorted.get( 0 );
//							final Card c2 = sorted.get( 2 );
//							if( (c0.rank == Rank.R_6 && c2.rank == Rank.R_8)
//									|| (c0.rank == Rank.R_7 && c2.rank == Rank.R_7) ) {
//								if( c0.suit == c1.suit && c1.suit == c2.suit ) {
//									// Suited
//									if( c0.suit == Suit.S_s ) {
//										// Spades pay 3:1
//										r += 3*s.player_hand.bets[i];
//										continue;
//									}
//									else {
//										// Non-spades pay 2:1
//										r += 2*s.player_hand.bets[i];
//										continue;
//									}
//								}
//								else {
//									// Not suited pays 3:2
//									r += 3*s.player_hand.bets[i] / 2;
//									continue;
//								}
//							}
//						}
//					}
//				}
//
//				// No bonuses, but player 21 always wins
//				r += s.player_hand.bets[i];
//			}
//			// Dealer bust or player beats dealer
//			else if( dv[0] > SpBjHand.max_score || pvi[0] > dv[0] ) {
//				r += s.player_hand.bets[i];
//			}
//			// Dealer beats player
//			else if( pvi[0] < dv[0] ) {
//				r += -s.player_hand.bets[i];
//			}
//			else {
//				assert( pvi[0] == dv[0] );
//				// Ties are pushes
//			}
//		}
//	}
	
//	private void uncompleteDealerHand()
//	{
//		final ArrayList<Card> hand = s.dealerHand();
//		while( hand.size() > 1 ) {
//			s.deck().undeal( hand.remove( hand.size() - 1 ) );
//		}
//		r = 0;
//	}

	@Override
	public double[] reward()
	{
		return new double[] { s.r };
	}

	@Override
	public boolean isTerminalState()
	{
		return s.isTerminal();
	}

	@Override
	public long horizon()
	{
		return Long.MAX_VALUE;
	}

	@Override
	public String detailString()
	{
		return "spanish-blackjack";
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv ) throws IOException
	{
		final int seed = 43;
		final RandomGenerator rng = new MersenneTwister( seed );
//		final Deck deck = new InfiniteSpanishDeck( rng );
		
		// TODO: Debugging code
		final Deque<Card> stacked = new ArrayDeque<Card>();
		for( int i = 0; i < 20; ++i ) {
			stacked.push( Card.C_2c );
			stacked.push( Card.C_2d );
			stacked.push( Card.C_2h );
			stacked.push( Card.C_2s );
		}
		final Deck deck = new StackedDeck( stacked );
		
//		final ArrayList<ArrayList<Card>> test_hands = new ArrayList<ArrayList<Card>>();
//		test_hands.add( new ArrayList<Card>( Arrays.asList( Card.C_Kc, Card.C_9h, Card.C_2c ) ) );
//		test_hands.add( new ArrayList<Card>( Arrays.asList( Card.C_6c, Card.C_7h, Card.C_8c ) ) );
//		test_hands.add( new ArrayList<Card>( Arrays.asList( Card.C_6c, Card.C_7c, Card.C_8c ) ) );
//		test_hands.add( new ArrayList<Card>( Arrays.asList( Card.C_6s, Card.C_7s, Card.C_8s ) ) );
//		test_hands.add( new ArrayList<Card>( Arrays.asList( Card.C_7c, Card.C_7h, Card.C_7c ) ) );
//		test_hands.add( new ArrayList<Card>( Arrays.asList( Card.C_7c, Card.C_7c, Card.C_7c ) ) );
//		test_hands.add( new ArrayList<Card>( Arrays.asList( Card.C_7s, Card.C_7s, Card.C_7s ) ) );
//		test_hands.add( new ArrayList<Card>( Arrays.asList( Card.C_3c, Card.C_3c, Card.C_3c, Card.C_3c, Card.C_9c ) ) );
//		test_hands.add( new ArrayList<Card>( Arrays.asList( Card.C_3c, Card.C_3c, Card.C_3c, Card.C_3c, Card.C_3c, Card.C_6c ) ) );
//		test_hands.add( new ArrayList<Card>( Arrays.asList( Card.C_3c, Card.C_3c, Card.C_3c, Card.C_3c, Card.C_3c, Card.C_3c, Card.C_3c ) ) );
//		test_hands.add( new ArrayList<Card>( Arrays.asList( Card.C_3c, Card.C_3c, Card.C_3c, Card.C_3c, Card.C_3c, Card.C_3c, Card.C_2c, Card.C_Ac ) ) );
//
//		final ArrayList<ArrayList<Card>> dealer_test_hands = new ArrayList<ArrayList<Card>>();
//		dealer_test_hands.add( new ArrayList<Card>( Arrays.asList( Card.C_Kc, Card.C_Kc ) ) );
//		dealer_test_hands.add( new ArrayList<Card>( Arrays.asList( Card.C_Kc, Card.C_Ac ) ) );
//
//		for( final ArrayList<Card> dealer_cards : dealer_test_hands ) {
//			for( final ArrayList<Card> cards : test_hands ) {
//				final SpBjState s = new SpBjState( deck );
//				s.init();
//				s.dealer_hand.clear();
//				s.dealer_hand.addAll( dealer_cards );
//				s.player_hand.hands.set( 0, cards );
//				final SpBjSimulator sim = new SpBjSimulator( s );
//				sim.takeAction( new JointAction<SpBjAction>(
//					new SpBjAction( new SpBjActionCategory[] { SpBjActionCategory.Pass } ) ) );
//
//				System.out.print( "Hand: " );
//				System.out.print( sim.state().player_hand );
//				System.out.print( " (" );
//				final ArrayList<int[]> values = sim.state().player_hand.values();
//				for( int i = 0; i < values.size(); ++i ) {
//					if( i > 0 ) {
//						System.out.print( ", " );
//					}
//					System.out.print( Arrays.toString( values.get( i ) ) );
//				}
//				System.out.println( ")" );
//
//				System.out.print( "Reward: " );
//				System.out.println( Arrays.toString( sim.reward() ) );
//				System.out.print( "Dealer hand: " );
//				System.out.print( sim.state().dealerHand().toString() );
//				System.out.print( " (" );
//				System.out.print( SpBjHand.handValue( sim.state().dealerHand() )[0] );
//				System.out.println( ")" );
//				System.out.println( "----------------------------------------" );
//			}
//		}
		
		while( true ) {
			final SpBjState s = new SpBjState( deck );
			s.init();
			final SpBjSimulator sim = new SpBjSimulator( s );

			final BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
			while( !s.isTerminal() ) {
				System.out.print( "Dealer showing: " );
				System.out.println( sim.state().dealerUpcard() );

				System.out.print( "Hand: " );
				System.out.print( sim.state().player_hand );
				System.out.print( " (" );
				final ArrayList<int[]> values = sim.state().player_hand.values();
				for( int i = 0; i < values.size(); ++i ) {
					if( i > 0 ) {
						System.out.print( ", " );
					}
					System.out.print( Arrays.toString( values.get( i ) ) );
				}
				System.out.println( ")" );
				
				final SpBjActionGenerator actions = new SpBjActionGenerator();
				actions.setState( sim.state(), 0 );
				for( final SpBjAction a : Fn.in( actions ) ) {
					System.out.println( a );
				}

				final String cmd = reader.readLine();
				assert( cmd.length() == sim.state().player_hand.Nhands );
				final SpBjActionCategory[] cat = new SpBjActionCategory[cmd.length()];
				for( int i = 0; i < cmd.length(); ++i ) {
					final char c = cmd.charAt( i );
					if( 'h' == c ) {
						cat[i] = SpBjActionCategory.Hit;
					}
					else if( 'p' == c ) {
						cat[i] = SpBjActionCategory.Pass;
					}
					else if( 'd' == c ) {
						cat[i] = SpBjActionCategory.Double;
					}
					else if( 's' == c ) {
						cat[i] = SpBjActionCategory.Split;
					}
				}
				sim.takeAction( new JointAction<SpBjAction>( new SpBjAction( cat ) ) );
			}

			System.out.print( "Hand: " );
			System.out.print( sim.state().player_hand );
			System.out.print( " (" );
			final ArrayList<int[]> values = sim.state().player_hand.values();
			for( int i = 0; i < values.size(); ++i ) {
				if( i > 0 ) {
					System.out.print( ", " );
				}
				System.out.print( Arrays.toString( values.get( i ) ) );
			}
			System.out.println( ")" );

			System.out.print( "Reward: " );
			System.out.println( Arrays.toString( sim.reward() ) );
			System.out.print( "Dealer hand: " );
			System.out.print( sim.state().dealerHand().toString() );
			System.out.print( " (" );
			System.out.print( SpBjHand.handValue( sim.state().dealerHand() )[0] );
			System.out.println( ")" );
			System.out.println( "----------------------------------------" );
		}
	}
}
