/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ArrayFactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * Quantizes a base FactoredRepresenter.
 * 
 * @author jhostetler
 */
public class QuantizedRepresenter<S> implements FactoredRepresenter<S, FactoredRepresentation<S>>
{
	private final FactoredRepresenter<S, ? extends FactoredRepresentation<S>> base_repr;
	private final double[][] thresholds;
	
	/**
	 * @param base_repr
	 * @param thresholds A array of arrays of quantization points. If an
	 * attribute i should not be quantized, then set thresholds[i] = [].
	 */
	public QuantizedRepresenter(
		final FactoredRepresenter<S, ? extends FactoredRepresentation<S>> base_repr, final double[][] thresholds )
	{
		this.base_repr = base_repr;
		this.thresholds = thresholds;
	}
	
	@Override
	public FactoredRepresenter<S, FactoredRepresentation<S>> create()
	{
		return new QuantizedRepresenter<S>( base_repr.create(), thresholds );
	}

	/**
	 * Represents a state as a vector of quantile indices.
	 * @see edu.oregonstate.eecs.mcplan.FactoredRepresenter#encode(java.lang.Object)
	 */
	@Override
	public ArrayFactoredRepresentation<S> encode( final S s )
	{
		final double[] q = new double[base_repr.attributes().size()];
		final double[] x = base_repr.encode( s ).phi();
		loop_x: for( int i = 0; i < x.length; ++i ) {
			for( int j = 0; j < thresholds[i].length - 1; ++j ) {
				if( x[i] < thresholds[i][j] ) {
					q[i] = j;
					continue loop_x;
				}
			}
			q[i] = thresholds[i].length;
		}
		return new ArrayFactoredRepresentation<S>( q );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		// FIXME: Should the attributes be different?
		return base_repr.attributes();
	}
}
