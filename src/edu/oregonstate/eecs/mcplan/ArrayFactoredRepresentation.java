/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.Arrays;

/**
 * @author jhostetler
 *
 */
public class ArrayFactoredRepresentation<S> extends FactoredRepresentation<S>
{
	private final double[] phi_;
	
	public ArrayFactoredRepresentation( final double[] phi )
	{
		phi_ = phi;
	}
	
	@Override
	public double[] phi()
	{
		return phi_;
	}

	@Override
	public Representation<S> copy()
	{
		return new ArrayFactoredRepresentation<S>( Arrays.copyOf( phi_, phi_.length ) );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof ArrayFactoredRepresentation) ) {
			return false;
		}
		@SuppressWarnings( "unchecked" )
		final ArrayFactoredRepresentation<S> that = (ArrayFactoredRepresentation<S>) obj;
		return Arrays.equals( phi_, that.phi_ );
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( phi_ );
	}

}
