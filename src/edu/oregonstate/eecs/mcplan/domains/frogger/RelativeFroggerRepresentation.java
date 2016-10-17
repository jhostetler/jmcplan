/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.frogger;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;

/**
 * @author jhostetler
 *
 */
public class RelativeFroggerRepresentation extends FactoredRepresentation<FroggerState>
{
	private final float[] phi_;
	
	public RelativeFroggerRepresentation( final FroggerState s, final int vision )
	{
		final int Npos = (2*vision + 1)*(2*vision + 1) - 1;
		phi_ = new float[2 + Npos];
		int idx = 0;
		phi_[idx++] = s.frog_x;
		phi_[idx++] = s.frog_y;
		for( int i = vision; i >= -vision; --i ) {
			for( int j = -vision; j <= vision; ++j ) {
				if( i == 0 && j == 0 ) {
					continue;
				}
				final int dx = j + s.frog_x;
				final int dy = i + s.frog_y;
				if( dx >= 0 && dx < s.params.road_length && dy >= 1 && dy <= s.params.lanes ) {
					phi_[idx] = s.grid[dy][dx] == Tile.Car ? 1 : 0; // fv[2 + (dy-1)*road_length + dx]
				}
				idx += 1;
			}
		}
		assert( idx == phi_.length );
	}
	
	private RelativeFroggerRepresentation( final RelativeFroggerRepresentation that )
	{
		phi_ = Arrays.copyOf( that.phi_, that.phi_.length );
	}
	
	@Override
	public float[] phi()
	{
		return phi_;
	}

	@Override
	public RelativeFroggerRepresentation copy()
	{
		return new RelativeFroggerRepresentation( this );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof RelativeFroggerRepresentation) ) {
			return false;
		}
		final RelativeFroggerRepresentation that = (RelativeFroggerRepresentation) obj;
		return Arrays.equals( phi_, that.phi_ );
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( phi_ );
	}
}
