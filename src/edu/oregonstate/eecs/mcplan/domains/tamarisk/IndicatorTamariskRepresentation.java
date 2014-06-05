/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tamarisk;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.Representation;

/**
 * @author jhostetler
 *
 */
public class IndicatorTamariskRepresentation extends FactoredRepresentation<TamariskState>
{
	private final double[] phi_;
	
	public IndicatorTamariskRepresentation( final TamariskState s )
	{
		phi_ = new double[2 * s.params.Nreaches * s.params.Nhabitats];
		int idx = 0;
		for( int r = 0; r < s.params.Nreaches; ++r ) {
			final Species[] reach = s.habitats[r];
			for( int h = 0; h < s.params.Nhabitats; ++h ) {
				final Species species = reach[h];
				if( species == Species.Native ) {
					phi_[idx] = 1.0;
				}
				else if( species == Species.Tamarisk ) {
					phi_[idx + 1] = 1.0;
				}
				idx += 2;
			}
		}
		assert( idx == phi_.length );
	}
	
	private IndicatorTamariskRepresentation( final IndicatorTamariskRepresentation that )
	{
		phi_ = Arrays.copyOf( that.phi_, that.phi_.length );
	}
	
	@Override
	public double[] phi()
	{
		return phi_;
	}

	@Override
	public Representation<TamariskState> copy()
	{
		return new IndicatorTamariskRepresentation( this );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof IndicatorTamariskRepresentation) ) {
			return false;
		}
		final IndicatorTamariskRepresentation that = (IndicatorTamariskRepresentation) obj;
		return Arrays.equals( phi_, that.phi_ );
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( phi_ );
	}
}
