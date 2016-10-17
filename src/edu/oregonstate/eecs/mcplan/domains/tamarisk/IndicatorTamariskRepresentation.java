/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tamarisk;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;

/**
 * @author jhostetler
 *
 */
public class IndicatorTamariskRepresentation extends FactoredRepresentation<TamariskState>
{
	private final float[] phi_;
	
	public IndicatorTamariskRepresentation( final TamariskState s )
	{
		phi_ = new float[2 * s.params.Nreaches * s.params.Nhabitats];
		int idx = 0;
		for( int r = 0; r < s.params.Nreaches; ++r ) {
			final Species[] reach = s.habitats[r];
			for( int h = 0; h < s.params.Nhabitats; ++h ) {
				final Species species = reach[h];
				if( species == Species.Native ) {
					phi_[idx] = 1.0f;
				}
				else if( species == Species.Tamarisk ) {
					phi_[idx + 1] = 1.0f;
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
	public float[] phi()
	{
		return phi_;
	}

	@Override
	public IndicatorTamariskRepresentation copy()
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
