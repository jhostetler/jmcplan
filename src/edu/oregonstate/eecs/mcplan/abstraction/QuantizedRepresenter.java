/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
