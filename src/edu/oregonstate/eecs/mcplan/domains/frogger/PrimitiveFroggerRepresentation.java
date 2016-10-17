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
public class PrimitiveFroggerRepresentation extends FactoredRepresentation<FroggerState>
{
	private final float[] phi_;
	
	public PrimitiveFroggerRepresentation( final FroggerState s )
	{
		phi_ = new float[2 + s.params.lanes*s.params.road_length];
		int idx = 0;
		phi_[idx++] = s.frog_x;
		phi_[idx++] = s.frog_y;
		for( int i = 0; i < s.params.lanes; ++i ) {
			for( int j = 0; j < s.params.road_length; ++j ) {
				phi_[idx++] = (s.grid[i+1][j] == Tile.Car ? 1 : 0);
			}
		}
	}
	
	private PrimitiveFroggerRepresentation( final PrimitiveFroggerRepresentation that )
	{
		phi_ = Arrays.copyOf( that.phi_, that.phi_.length );
	}
	
	@Override
	public float[] phi()
	{
		return phi_;
	}

	@Override
	public PrimitiveFroggerRepresentation copy()
	{
		return new PrimitiveFroggerRepresentation( this );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof PrimitiveFroggerRepresentation) ) {
			return false;
		}
		final PrimitiveFroggerRepresentation that = (PrimitiveFroggerRepresentation) obj;
		return Arrays.equals( phi_, that.phi_ );
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( phi_ );
	}
}