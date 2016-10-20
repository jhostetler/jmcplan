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
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import java.io.IOException;
import java.util.Arrays;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

/**
 * Encodes an MWNumericArray with any number of dimensions into a nested
 * JsonArray.
 * @author jhostetler
 */
public class MWNumericArrayJsonSerializer extends TypeAdapter<MWNumericArray>
{

	@Override
	public MWNumericArray read( final JsonReader reader ) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void write( final JsonWriter writer, final MWNumericArray mw ) throws IOException
	{
		if( mw == null ) {
			writer.nullValue();
			return;
		}
		
		reduceDimension( writer, mw, new int[0] );
	}
	
	private void reduceDimension( final JsonWriter writer, final MWNumericArray mw, final int[] idx ) throws IOException
	{
		final int[] N = mw.getDimensions();
		if( idx.length == N.length ) {
			final double d = mw.getDouble( idx );
			if( Double.isNaN( d  ) ) {
				writer.value( "NaN" );
			}
			else if( d == Double.POSITIVE_INFINITY ) {
				writer.value( "Inf" );
			}
			else if( d == Double.NEGATIVE_INFINITY ) {
				writer.value( "-Inf" );
			}
			else {
				writer.value( mw.getDouble( idx ) );
			}
		}
		else {
			final int[] idx_prime = Arrays.copyOf( idx, idx.length + 1 );
			writer.beginArray();
			for( int i = 1; i <= N[idx.length]; ++i ) {
				idx_prime[idx.length] = i;
				reduceDimension( writer, mw, idx_prime );
			}
			writer.endArray();
		}
	}
}
