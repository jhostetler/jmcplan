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
	public static RacegridState barto_bradtke_singh_SmallTrack( final RandomGenerator rng, final int scale )
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
		return initState( rng, terrain );
	}
	
	public static RacegridState barto_bradtke_singh_LargeTrack( final RandomGenerator rng, final int scale )
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
		return initState( rng, terrain );
	}
	
	private static RacegridState initState( final RandomGenerator rng, final TerrainType[][] terrain )
	{
		final RacegridState s = new RacegridState( terrain );
		
		final int start_idx = rng.nextInt( s.starts.size() );
		final int[] start = s.starts.get( start_idx );
		s.x = start[0];
		s.y = start[1];
		
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
