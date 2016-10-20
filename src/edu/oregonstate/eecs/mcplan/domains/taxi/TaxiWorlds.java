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
