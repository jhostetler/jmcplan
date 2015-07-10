/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.Arrays;


/**
 * A Representation that additionally represents the state as a feature
 * vector in R^N.
 */
public abstract class FactoredRepresentation<S> extends Representation<S>
{
	/**
	 * Returns the feature vector representation.
	 * @return
	 */
	public abstract double[] phi();
	
	@Override
	public abstract FactoredRepresentation<S> copy();
	
	@Override
	public final boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof FactoredRepresentation) ) {
			return false;
		}
		@SuppressWarnings( "unchecked" )
		final FactoredRepresentation<S> that = (ArrayFactoredRepresentation<S>) obj;
		return Arrays.equals( phi(), that.phi() );
	}

	@Override
	public final int hashCode()
	{
		return Arrays.hashCode( phi() );
	}
	
	@Override
	public String toString()
	{
		return Arrays.toString( phi() );
	}
}
