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
			mw.write( writer, a.toMatlab( params, t_default ) );
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
