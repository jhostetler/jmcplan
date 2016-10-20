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
package edu.oregonstate.eecs.mcplan.domains.racegrid;

import org.apache.commons.math3.random.RandomGenerator;


/**
 * @author jhostetler
 *
 */
public class RacegridCircuits
{
	public static RacegridState barto_bradtke_singh_SmallTrack( final RandomGenerator rng, final int T, final int scale )
	{
		final String[] spec = new String[] {
			"wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwgggw",
			"wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwtttw",
			"wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwtttw",
			"wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwtttw",
			"wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwtttw",
			"sttttttttttttttttttttttttttttttttttttttw",
			"sttttttttttttttttttttttttttttttttttttttw",
			"sttttttttttttttttttttttttttttttttttttttw",
			"sttttttttttttttttttttttttttttttttttttttw",
			"wwwwtttttttttttttttttttttttttttttttttttw",
			"wwwwwwwwtttttttttttttttttttttttttttttttw",
			"wwwwwwwwwwwwtttttttttttttttttttttttttttw",
			"wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww"
		};
		final TerrainType[][] terrain = parse( spec, scale );
		return initState( rng, terrain, T );
	}
	
	public static RacegridState barto_bradtke_singh_LargeTrack( final RandomGenerator rng, final int T, final int scale )
	{
		final String[] spec = new String[] {
			"wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww",
			"wwwwwwwwwwwtttttttwwwwwwwwwwwwww",
			"wwwwwwwttttttttttttttwwwwwwwwwww",
			"wwwtttttttttttttttttttttwwwwwwww",
			"wwwttttttttttttttttttttttttwwwww",
			"wwwttttttttttttttttttttttttwwwww",
			"wwwttttttttttttttttttttttttwwwww",
			"wwwttttttttttwwttttttttttttwwwww",
			"wwwttttttttwwwwwtttttttttttwwwww",
			"wwwtttttttwwwwwwwttttttttttwwwww",
			"wwwttttttwwwwwttttttttttttwwwwww",
			"wwwtttttwwwwttttttttttttwwwwwwww",
			"wwwtttttwwwwttttttttttwwwwwwwwww",
			"wwttttttwwwwttttttttwwwwwwwwwwww",
			"wwttttttwwwwttttttwwwwwwwwwwwwww",
			"wwttttttwwwwtttttttttttttwwwwwww",
			"wwttttttwwwtttttttttttttttwwwwww",
			"wwtttttwwwwttttttttttttttttwwwww",
			"wwtttttwwwwtttttttttttttttttwwww",
			"wwtttttwwwwttttttttttttttttttwww",
			"wwtttttwwwwwttttttttttttttttttww",
			"wwtttttwwwwwwttttttttttttttttttw",
			"wwtttttwwwwwwwwwwwwwwwwttttttttw",
			"wttttttwwwwwwwwwwwwwwwwwtttttttw",
			"wttttttwwwwwwwwwwwwwwwwwtttttttw",
			"wttttttwwwwwwwwwwwwwwwwwtttttttw",
			"wttttttwwwwwwwwwwwwwwwwwtttttttw",
			"wttttttwwwwwwwwwwwwwwwwwtttttttw",
			"wttttttwwwwwwwwwwwwwwwwwtttttttw",
			"wttttttwwwwwwwwwwwwwwwwwtttttttw",
			"wttttttwwwwwwwwwwwwwwwwwtttttttw",
			"wttttttwwwwwwwwwwwwwwwwwtttttttw",
			"wttttttwwwwwwwwwwwwwwwwwtttttttw",
			"wsssssswwwwwwwwwwwwwwwwwgggggggw"
		};
		final TerrainType[][] terrain = parse( spec, scale );
		return initState( rng, terrain, T );
	}
	
	private static RacegridState initState( final RandomGenerator rng, final TerrainType[][] terrain, final int T )
	{
		final RacegridState s = new RacegridState( terrain, T );
		s.setRandomStartState( rng );
		
//		final int start_idx = rng.nextInt( s.starts.size() );
//		final int[] start = s.starts.get( start_idx );
//		s.x = start[0];
//		s.y = start[1];
		
		return s;
	}
	
	private static TerrainType[][] parse( final String[] spec, final int scale )
	{
		final TerrainType[][] terrain = new TerrainType[spec.length*scale][];
		final int height = spec.length;
		final int width = spec[0].length();
		for( int i = 0; i < spec.length; ++i ) {
//			assert( terrain[i].length == width );
			for( int ii = 0; ii < scale; ++ii ) {
				// We use 'height - i - 1' to invert the y-axis, so that y
				// increases up and the track looks the same as the string array.
				terrain[i*scale + ii] = new TerrainType[spec[height - i - 1].length()*scale];
				for( int j = 0; j < spec[height - i - 1].length(); ++j ) {
					final char ij = spec[height - i - 1].charAt( j );
					for( int jj = 0; jj < scale; ++jj ) {
						if( 't' ==  ij ) {
							terrain[i*scale + ii][j*scale + jj] = TerrainType.Track;
						}
						else if( 's' == ij ) {
							terrain[i*scale + ii][j*scale + jj] = TerrainType.Start;
						}
						else if( 'g' == ij ) {
							terrain[i*scale + ii][j*scale + jj] = TerrainType.Goal;
						}
						else if( 'w' == ij ) {
							terrain[i*scale + ii][j*scale + jj] = TerrainType.Wall;
						}
						else {
							throw new IllegalArgumentException( "spec[" + i + "][" + j + "] = " + spec[i].charAt( j ) );
						}
					}
				}
			}
		}
		
		return terrain;
	}
}
