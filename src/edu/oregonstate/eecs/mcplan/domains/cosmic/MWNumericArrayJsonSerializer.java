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
