/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.frogger;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.Representation;

/**
 * @author jhostetler
 *
 */
public class RelativeFroggerRepresentation extends FactoredRepresentation<FroggerState>
{
	private final double[] phi_;
	
	public RelativeFroggerRepresentation( final FroggerState s )
	{
		final int road_vision = s.params.road_length - 1;
		final int Nx = 2*road_vision + 1;
		final int Ny = (s.params.lanes-1) + s.params.lanes + 1;
		final int Npos = Nx*Ny;
		phi_ = new double[2 + Npos];
		int idx = 0;
		phi_[idx++] = s.frog_x;
		phi_[idx++] = s.frog_y;
		for( int i = s.params.lanes; i >= -s.params.lanes + 1; --i ) {
			for( int j = -road_vision; j <= road_vision; ++j ) {
				final int dx = j + s.frog_x;
				final int dy = i + s.frog_y;
				if( dx >= 0 && dx < s.params.road_length && dy >= 1 && dy <= s.params.lanes ) {
					phi_[idx] = s.grid[dy][dx] == Tile.Car ? 1.0 : 0.0; // fv[2 + (dy-1)*road_length + dx]
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
	public double[] phi()
	{
		return phi_;
	}

	@Override
	public Representation<FroggerState> copy()
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
