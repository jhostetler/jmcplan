/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.taxi;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.Representation;

/**
 * @author jhostetler
 *
 */
public class PrimitiveTaxiRepresentation extends FactoredRepresentation<TaxiState>
{
	private final double[] phi_;
	
	public PrimitiveTaxiRepresentation( final TaxiState s )
	{
		// Taxi location, other taxi locations, passenger location indicators +
		// destination indicators.
		final int Nfeatures = 2 + 2*s.Nother_taxis + (2*s.locations.size() + 1);
		phi_ = new double[Nfeatures];
		int idx = 0;
		phi_[idx++] = s.taxi[0];
		phi_[idx++] = s.taxi[1];
		for( final int[] other : s.other_taxis ) {
			phi_[idx++] = other[0];
			phi_[idx++] = other[1];
		}
		phi_[idx + s.passenger + 1] = 1.0;
		idx += s.locations.size() + 1;
		phi_[idx + s.destination] = 1.0;
		idx += s.locations.size();
		
		assert( idx == Nfeatures );
	}
	
	private PrimitiveTaxiRepresentation( final PrimitiveTaxiRepresentation that )
	{
		this.phi_ = that.phi_;
	}
	
	@Override
	public double[] phi()
	{
		return phi_;
	}

	@Override
	public Representation<TaxiState> copy()
	{
		return new PrimitiveTaxiRepresentation( this );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof PrimitiveTaxiRepresentation) ) {
			return false;
		}
		final PrimitiveTaxiRepresentation that = (PrimitiveTaxiRepresentation) obj;
		return Arrays.equals( phi_, that.phi_ );
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( phi_ );
	}
}
