/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.taxi;

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class TaxiWorlds
{
	public static TaxiState dietterich2000( final RandomGenerator rng, final int Nother_taxis, final double slip )
	{
		// Topology
		
		final int wr = TaxiState.wall_right;
		final int wu = TaxiState.wall_up;
	
		final int[][] topology = new int[][] {
			{ 0,		wr,		0,		0,		wr },
			{ 0,		wr,		0,		0,		wr },
			{ 0,		0,		0,		0,		wr },
			{ wr,		0,		wr,		0,		wr },
			{ wr,		0,		wr,		0,		wr },
		};
		final int[][] flipped = new int[topology[0].length][topology.length];
		
		for( int i = 0; i < flipped.length; ++i ) {
			for( int j = 0; j < flipped[i].length; ++j ) {
				flipped[i][j] = topology[flipped[i].length - j - 1][i];
			}
		}
		
		final ArrayList<int[]> locations = new ArrayList<int[]>();
		locations.add( new int[] { 0, 0 } );
		locations.add( new int[] { 3, 0 } );
		locations.add( new int[] { 0, 4 } );
		locations.add( new int[] { 4, 4 } );
		
		// Random starting conditions
		
		final TaxiState s = new TaxiState( flipped, locations, Nother_taxis, slip );
		s.passenger = rng.nextInt( s.locations.size() );
		s.destination = rng.nextInt( s.locations.size() );
		
		s.taxi[0] = rng.nextInt( s.width );
		s.taxi[1] = rng.nextInt( s.height );
		
		for( int i = 0; i < s.Nother_taxis; ++i ) {
			final int[] pos = new int[2];
			do {
				pos[0] = rng.nextInt( s.width );
				pos[1] = rng.nextInt( s.height );
			}
			while( s.isOccupied( pos, i ) );
			Fn.memcpy( s.other_taxis[i], pos, 2 );
		}
		
		return s;
	}
}
