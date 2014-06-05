/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racegrid;


/**
 * @author jhostetler
 *
 */
public class RacegridCircuits
{
	public static TerrainType[][] barto_bradke_singh_SmallTrack()
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
		return parse( spec );
	}
	
	public static TerrainType[][] barto_bradke_singh_LargeTrack()
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
		return parse( spec );
	}
	
	private static TerrainType[][] parse( final String[] spec )
	{
		final TerrainType[][] terrain = new TerrainType[spec.length][];
		final int height = spec.length;
		final int width = spec[0].length();
		for( int i = 0; i < spec.length; ++i ) {
			// We use 'height - i - 1' to invert the y-axis, so that y
			// increases up and the track looks the same as the string array.
			terrain[i] = new TerrainType[spec[height - i - 1].length()];
			assert( terrain[i].length == width );
			for( int j = 0; j < spec[height - i - 1].length(); ++j ) {
				final char ij = spec[height - i - 1].charAt( j );
				if( 't' ==  ij ) {
					terrain[i][j] = TerrainType.Track;
				}
				else if( 's' == ij ) {
					terrain[i][j] = TerrainType.Start;
				}
				else if( 'g' == ij ) {
					terrain[i][j] = TerrainType.Goal;
				}
				else if( 'w' == ij ) {
					terrain[i][j] = TerrainType.Wall;
				}
				else {
					throw new IllegalArgumentException( "spec[" + i + "][" + j + "] = " + spec[i].charAt( j ) );
				}
			}
		}
		
		return terrain;
	}
}
