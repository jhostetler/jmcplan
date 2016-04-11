/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

/**
 * @author jhostetler
 *
 */
public final class CosmicGson
{
	private CosmicGson()
	{ }
	
	/**
	 * Convenience method to register appropriate type adapters for Cosmic.
	 * @param builder
	 * @return
	 */
	public static Gson createGson( final CosmicParameters params, final GsonBuilder builder )
	{
		builder.registerTypeAdapter( MWNumericArray.class, new MWNumericArrayJsonSerializer() );
		builder.registerTypeAdapterFactory( new CosmicState.GsonSerializer() );
		builder.registerTypeAdapterFactory( new CosmicAction.GsonSerializer( params ) );
		return builder.create();
	}
}
