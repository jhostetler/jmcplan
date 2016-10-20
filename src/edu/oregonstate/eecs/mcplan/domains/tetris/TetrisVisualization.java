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
package edu.oregonstate.eecs.mcplan.domains.tetris;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public class TetrisVisualization
{

	/**
	 * @param args
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public static void main( final String[] args ) throws NumberFormatException, IOException
	{
		
//		final int[][] cells = new int[][] {
//			new int[] { 1, 2, 3, 0, 4, 5, 0, 0, 0, 6 },
//			new int[] { 0, 7, 0, 0, 8, 9, 0, 0, 0, 1 },
//			new int[] { 2, 3, 4, 5, 0, 0, 0, 0, 4, 3 },
//			new int[] { 0, 0, 0, 0, 1, 2, 0, 0, 2, 9 },
//			new int[] { 0, 0, 0, 0, 3, 4, 0, 0, 8, 7 },
//			new int[] { 0, 0, 0, 0, 0, 0, 0, 5, 0, 0 },
//			new int[] { 0, 0, 0, 0, 0, 0, 0, 5, 0, 0 },
//			new int[] { 0, 0, 0, 0, 0, 0, 0, 5, 0, 0 },
//			new int[] { 0, 0, 0, 0, 0, 0, 0, 5, 0, 0 }
//		};
		
		// This one tests cascading block falls
//		final int[][] cells = new int[][] {
//			new int[] { 2, 2, 2, 2, 2, 2, 0, 2, 2, 2 },
//			new int[] { 3, 0, 0, 1, 8, 9, 1, 1, 1, 1 },
//			new int[] { 0, 0, 0, 0, 0, 0, 1, 0, 1, 3 },
//			new int[] { 0, 4, 4, 0, 0, 0, 1, 0, 1, 9 },
//			new int[] { 0, 0, 4, 0, 0, 0, 0, 1, 8, 7 },
//			new int[] { 0, 0, 4, 0, 0, 0, 0, 0, 0, 0 },
//			new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
//			new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
//			new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }
//		};
		
//		for( int y = 0; y < cells.length; ++y ) {
//			for( int x = 0; x < TetrisState.Ncolumns; ++x ) {
//				s.cells[y][x] = cells[y][x];
//			}
//		}
		
		final int T = 50;
		final int Nrows = 10;
		final RandomGenerator rng = new MersenneTwister( 43 );
		final TetrisParameters params = new TetrisParameters( T, Nrows );
		final TetrisFsssModel model = new TetrisFsssModel( rng, params, new TetrisBertsekasRepresenter( params ) );
		TetrisState s = model.initialState();
		
//		for( final TetrominoType type : TetrominoType.values() ) {
//			final Tetromino t = type.create();
//			for( int r = 0; r < 4; ++r ) {
//				for( int x = 0; x < TetrisState.Ncolumns; ++x ) {
//					s = model.initialState();
////					Fn.assign( s.cells, 0 );
//					s.queued_tetro = t;
//
//					System.out.println( "" + type + ", " + x + ", " + r );
//
//					final TetrisAction a = new TetrisAction( x, r );
//					a.doAction( s );
//
////					try {
////						t.setCells( s, 1 );
////					}
////					catch( final TetrisGameOver ex ) {
////						// TODO Auto-generated catch block
////						ex.printStackTrace();
////					}
//					System.out.println( s );
//				}
//			}
//		}
		

		int steps = 0;
		while( !s.isTerminal() ) {
			System.out.println( s );
			System.out.println( model.base_repr().encode( s ) );

//			final int t = rng.nextInt( TetrominoType.values().length );
//			final int r = rng.nextInt( 4 );
//			final Tetromino tetro = TetrominoType.values()[t].create();
//			tetro.setRotation( r );
//			System.out.println( "Next:" );
//			System.out.println( tetro );

//			final int input_position = rng.nextInt( params.Ncolumns );
//			final int input_rotation = rng.nextInt( 4 );
			
			// This move sequence produces a cascading clear for seed=43:
			// 00 41 21 60 91 73 41 01 01 61 83 53 23 31
			
			System.out.print( ">>> " );
			final BufferedReader cin = new BufferedReader( new InputStreamReader( System.in ) );
			final int choice = Integer.parseInt( cin.readLine() );
			final int input_position = choice / 10;
			final int input_rotation = choice - (input_position*10);

			final TetrisAction a = new TetrisAction( input_position, input_rotation );
			System.out.println( "Input: " + a );

			final TetrisState sprime = model.sampleTransition( s, a );
			s = sprime;

//			tetro.setPosition( input_position, TetrisState.Nrows - 1 - tetro.getBoundingBox().top );
//			tetro.setRotation( input_rotation );
//			s.createTetromino( tetro );

			++steps;

			if( s.isTerminal() ) {
				break;
			}

//			System.out.println( s );
//			System.out.println();
//			final int clears = s.drop();
//			if( clears > 0 ) {
//				System.out.println( "\tCleared " + clears );
//			}
		}

		System.out.println( "Steps: " + steps );

	}

}
