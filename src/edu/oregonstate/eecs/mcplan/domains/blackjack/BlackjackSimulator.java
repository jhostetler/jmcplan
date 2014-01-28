/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;

/**
 * @author jhostetler
 *
 */
public class BlackjackSimulator implements UndoSimulator<BlackjackState, BlackjackAction>
{
	private final BlackjackState s_;
	private final double[] r_;
	
	private final Deque<JointAction<BlackjackAction>> action_history_
		= new ArrayDeque<JointAction<BlackjackAction>>();
	
	public BlackjackSimulator( final Deck deck, final int nplayers )
	{
		s_ = new BlackjackState( deck, nplayers );
		r_ = new double[nplayers];
	}
	
	@Override
	public BlackjackState state()
	{
		return s_;
	}

	@Override
	public void takeAction( final JointAction<BlackjackAction> a )
	{
		for( final BlackjackAction ai : a ) {
			ai.doAction( s_ );
		}
		action_history_.push( a );
		
		if( isTerminalState() ) {
			completeDealerHand();
		}
	}

	@Override
	public void untakeLastAction()
	{
		if( isTerminalState() ) {
			uncompleteDealerHand();
		}
		
		final JointAction<BlackjackAction> a = action_history_.pop();
		for( final BlackjackAction ai : a ) {
			ai.undoAction( s_ );
		}
	}

	@Override
	public long depth()
	{
		return action_history_.size();
	}

	@Override
	public long t()
	{
		return action_history_.size();
	}

	@Override
	public int nagents()
	{
		return s_.nplayers();
	}

	@Override
	public int[] turn()
	{
		for( int i = 0; i < s_.nplayers(); ++i ) {
			if( !s_.passed( i ) ) {
				return new int[] { i };
			}
		}
		return new int[] { };
	}
	
	private void completeDealerHand()
	{
		final ArrayList<Card> hand = s_.dealerHand();
		int[] dv = null;
		while( true ) {
			if( hand.size() < 2 ) {
				hand.add( s_.deck().deal() );
			}
			else {
				dv = Blackjack.handValue( hand );
				// Dealer stands on soft 17
				if( dv[0] <= 16 ) { //|| (dv[1] > 0 && dv[0] <= 17) ) {
					hand.add( s_.deck().deal() );
				}
				else {
					break;
				}
			}
		}
		
		for( int i = 0; i < s_.nplayers(); ++i ) {
			final int[] pv = Blackjack.handValue( s_.hand( i ) );
			if( pv[0] > 21 ) {
				r_[i] = -1;
			}
			else if( dv[0] > 21 || pv[0] > dv[0] ) {
				r_[i] = 1;
			}
			else if( pv[0] < dv[0] ) {
				r_[i] = -1;
			}
			else {
				r_[i] = 0;
			}
		}
	}
	
	private void uncompleteDealerHand()
	{
		final ArrayList<Card> hand = s_.dealerHand();
		while( hand.size() > 1 ) {
			s_.deck().undeal( hand.remove( hand.size() - 1 ) );
		}
		Arrays.fill( r_, 0.0 );
	}

	@Override
	public double[] reward()
	{
		return Arrays.copyOf( r_, r_.length );
	}

	@Override
	public boolean isTerminalState()
	{
		for( int i = 0; i < s_.nplayers(); ++i ) {
			if( !s_.passed( i ) ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public long horizon()
	{
		return Long.MAX_VALUE;
	}

	@Override
	public String detailString()
	{
		return "blackjack";
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv ) throws IOException
	{
		while( true ) {
			final Deck deck = new InfiniteDeck();
			final BlackjackSimulator sim = new BlackjackSimulator( deck, 1 );
			
			System.out.print( "Dealer showing: " );
			System.out.println( sim.state().dealerUpcard() );
			
			final BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
			while( !sim.isTerminalState() ) {
				System.out.print( "Hand: " );
				System.out.print( sim.state().hand( 0 ) );
				System.out.print( " (" );
				System.out.print( Blackjack.handValue( sim.state().hand( 0 ) )[0] );
				System.out.println( ")" );
				
				final String cmd = reader.readLine();
				if( "h".equals( cmd ) ) {
					sim.takeAction( new JointAction<BlackjackAction>( new HitAction( 0 ) ) );
				}
				else {
					sim.takeAction( new JointAction<BlackjackAction>( new PassAction( 0 ) ) );
				}
			}
			
			System.out.print( "Hand: " );
			System.out.print( sim.state().hand( 0 ) );
			System.out.print( " (" );
			System.out.print( Blackjack.handValue( sim.state().hand( 0 ) )[0] );
			System.out.println( ")" );
			
			System.out.print( "Reward: " );
			System.out.println( Arrays.toString( sim.reward() ) );
			System.out.print( "Dealer hand: " );
			System.out.print( sim.state().dealerHand().toString() );
			System.out.print( " (" );
			System.out.print( Blackjack.handValue( sim.state().dealerHand() )[0] );
			System.out.println( ")" );
			System.out.println( "----------------------------------------" );
		}
	}

}
