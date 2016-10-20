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

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public abstract class CosmicAction implements VirtualConstructor<CosmicAction>
{
	public static class GsonSerializer implements TypeAdapterFactory
	{
		private final CosmicParameters params;
		
		public GsonSerializer( final CosmicParameters params )
		{
			this.params = params;
		}
		
		@SuppressWarnings( "unchecked" )
		@Override
		public <T> TypeAdapter<T> create( final Gson gson, final TypeToken<T> token )
		{
			if( !CosmicAction.class.isAssignableFrom( token.getRawType() ) ) {
				return null;
			}
			
			final TypeAdapter<?> mw = gson.getAdapter( TypeToken.get( MWNumericArray.class ) );
			return (TypeAdapter<T>) new GsonTypeAdapter( params, (TypeAdapter<MWNumericArray>) mw );
		}
	}
	
	public static class GsonTypeAdapter extends TypeAdapter<CosmicAction>
	{
		private final CosmicParameters params;
		private final TypeAdapter<MWNumericArray> mw;
		
		public GsonTypeAdapter( final CosmicParameters params, final TypeAdapter<MWNumericArray> mw )
		{
			this.params = params;
			this.mw = mw;
		}
		
		@Override
		public CosmicAction read( final JsonReader reader ) throws IOException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void write( final JsonWriter writer, final CosmicAction a ) throws IOException
		{
			final int t_default = -1;
			writer.beginObject();
			writer.name( "a" ).value( a.toString() );
			writer.name( "m" );
			MWNumericArray m = null;
			try {
				m = a.toMatlab( params, t_default );
				mw.write( writer, m );
			}
			finally {
				m.dispose();
			}
			writer.endObject();
		}
		
	}
	
	/**
	 * Returned array is owned by caller.
	 * @param params
	 * @param t
	 * @return
	 */
	public abstract MWNumericArray toMatlab( final CosmicParameters params, final double t );
	
	/**
	 * Apply changes to the state due to this action that are not modeled by
	 * the Cosmic system.
	 * @param sprime The CosmicState obtained by applying take_action
	 */
	public abstract void applyNonCosmicChanges( CosmicState sprime );
	
	@Override
	public abstract int hashCode();
	
	@Override
	public abstract boolean equals( final Object obj );
	
	@Override
	public abstract String toString();
}
