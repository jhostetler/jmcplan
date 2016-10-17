/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.fuelworld;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;

/**
 * @author jhostetler
 *
 */
public class PrimitiveFuelWorldRepresentation extends FactoredRepresentation<FuelWorldState>
{
	final float[] phi_;
	
	public PrimitiveFuelWorldRepresentation( final FuelWorldState s )
	{
		phi_ = new float[3];
		int idx = 0;
		phi_[idx++] = s.location;
		phi_[idx++] = s.fuel;
		phi_[idx++] = (s.fuel_depots.contains( s.location ) ? 1 : 0);
	}
	
	private PrimitiveFuelWorldRepresentation( final float[] phi )
	{
		phi_ = phi;
	}
	
	@Override
	public float[] phi()
	{
		return phi_;
	}

	@Override
	public PrimitiveFuelWorldRepresentation copy()
	{
		return new PrimitiveFuelWorldRepresentation( phi_ );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof PrimitiveFuelWorldRepresentation) ) {
			return false;
		}
		
		final PrimitiveFuelWorldRepresentation that = (PrimitiveFuelWorldRepresentation) obj;
		return Arrays.equals( phi_, that.phi_ );
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( phi_ );
	}
}
