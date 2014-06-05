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
public class PrimitiveTamariskRepresentation extends FactoredRepresentation<TamariskState>
{
	private final double[] phi_;
	
	public PrimitiveTamariskRepresentation( final TamariskState s )
	{
		phi_ = new double[s.params.Nreaches * s.params.Nhabitats];
		int idx = 0;
		for( int r = 0; r < s.params.Nreaches; ++r ) {
			final Species[] reach = s.habitats[r];
			for( int h = 0; h < s.params.Nhabitats; ++h ) {
				phi_[idx++] = reach[h].ordinal();
			}
		}
	}
	
	private PrimitiveTamariskRepresentation( final PrimitiveTamariskRepresentation that )
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
		return new PrimitiveTamariskRepresentation( this );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof PrimitiveTamariskRepresentation) ) {
			return false;
		}
		final PrimitiveTamariskRepresentation that = (PrimitiveTamariskRepresentation) obj;
		return Arrays.equals( phi_, that.phi_ );
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( phi_ );
	}
}
