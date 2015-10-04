/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tetris;

import java.io.IOException;

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
		
		final RandomGenerator rng = new MersenneTwister( 43 );
		final TetrisParameters params = new TetrisParameters( 10 );
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

			final int input_position = rng.nextInt( params.Ncolumns );
			final int input_rotation = rng.nextInt( 4 );
			final TetrisAction a = new TetrisAction( input_position, input_rotation );

//			System.out.print( ">>> " );
//			final BufferedReader cin = new BufferedReader( new InputStreamReader( System.in ) );
//			final int choice = Integer.parseInt( cin.readLine() );
//			final int input_position = choice / 10;
//			final int input_rotation = choice - (input_position*10);

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
