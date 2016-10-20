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
