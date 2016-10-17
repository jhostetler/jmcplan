/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.JointAction;

/**
 * @author jhostetler
 *
 */
public class YahtzeeClient
{

	private static void printState( final YahtzeeState s )
	{
		for( final YahtzeeScores category : YahtzeeScores.values() ) {
			System.out.format( "%-20s%s %3d\n",
							   category, (s.filled[category.ordinal()] ? "x" : ":"), s.scores[category.ordinal()] );
		}
		System.out.format( "Upper bonus:   %d\n", s.upper_bonus );
		System.out.format( "Yahtzee bonus: %d\n", s.yahtzee_bonus );
		System.out.format( "Total: ------> %d\n", s.score() );
		System.out.println( "Hand: " + Arrays.toString( s.hand().dice ) );
		
		final YahtzeeActionGenerator action_gen = new YahtzeeActionGenerator();
		action_gen.setState( s, 0L );
		while( action_gen.hasNext() ) {
			System.out.println( action_gen.next() );
		}
	}
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main( final String[] args ) throws IOException
	{
		final RandomGenerator rng = new MersenneTwister( 42 );
		final YahtzeeState s0 = new YahtzeeState( rng );
		final YahtzeeSimulator sim = new YahtzeeSimulator( rng, s0 );
		
		final BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
		while( !sim.state().isTerminal() ) {
			printState( sim.state() );
			final String cmd = reader.readLine();
			final String[] parts = cmd.split( " " );
			if( "keep".equals( parts[0] ) ) {
				final int[] keepers = new int[Hand.Nfaces];
				for( int i = 1; i < parts.length; ++i ) {
					final int v = Integer.parseInt( parts[i] );
					keepers[v - 1] += 1;
				}
				sim.takeAction( new JointAction<YahtzeeAction>( new KeepAction( keepers ) ) );
			}
			else if( "score".equals( parts[0] ) ) {
				final YahtzeeScores category = YahtzeeScores.valueOf( parts[1] );
				sim.takeAction( new JointAction<YahtzeeAction>( new ScoreAction( category ) ) );
			}
			else if( "undo".equals( parts[0] ) ) {
				sim.untakeLastAction();
			}
			else {
				System.out.println( "!!! Bad command" );
			}
		}
		System.out.println( "********** Terminal **********" );
		printState( sim.state() );
	}

}
