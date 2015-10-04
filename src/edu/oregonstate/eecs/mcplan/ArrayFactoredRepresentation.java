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
	private final float[] phi_;
	
	public ArrayFactoredRepresentation( final float[] phi )
	{
		phi_ = phi;
	}
	
	@Override
	public float[] phi()
	{
		return phi_;
	}

	@Override
	public ArrayFactoredRepresentation<S> copy()
	{
		return new ArrayFactoredRepresentation<S>( Arrays.copyOf( phi_, phi_.length ) );
	}

}
