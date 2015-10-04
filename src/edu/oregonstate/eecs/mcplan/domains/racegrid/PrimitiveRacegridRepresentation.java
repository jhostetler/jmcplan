/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racegrid;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;

/**
 * @author jhostetler
 *
 */
public class PrimitiveRacegridRepresentation extends FactoredRepresentation<RacegridState>
{
	private final float[] phi_;
	
	public PrimitiveRacegridRepresentation( final RacegridState s )
	{
		phi_ = new float[4];
		int idx = 0;
		phi_[idx++] = s.x;
		phi_[idx++] = s.y;
		phi_[idx++] = s.dx;
		phi_[idx++] = s.dy;
	}
	
	private PrimitiveRacegridRepresentation( final PrimitiveRacegridRepresentation that )
	{
		phi_ = Arrays.copyOf( that.phi_, that.phi_.length );
	}
	
	@Override
	public float[] phi()
	{
		return phi_;
	}

	@Override
	public PrimitiveRacegridRepresentation copy()
	{
		return new PrimitiveRacegridRepresentation( this );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof PrimitiveRacegridRepresentation) ) {
			return false;
		}
		final PrimitiveRacegridRepresentation that = (PrimitiveRacegridRepresentation) obj;
		return Arrays.equals( phi_, that.phi_ );
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( phi_ );
	}
}
