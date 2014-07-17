/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.taxi;

import java.util.ArrayList;

/**
 * @author jhostetler
 *
 */
public class TaxiWorlds
{
	public static TaxiState dietterich2000( final int Nother_taxis )
	{
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
		
		return new TaxiState( flipped, locations, Nother_taxis );
	}
}
