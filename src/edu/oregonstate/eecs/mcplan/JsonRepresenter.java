/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import com.google.gson.Gson;

/**
 * @author jhostetler
 *
 */
public class JsonRepresenter<S> implements Representer<S, JsonRepresentation<S>>
{
	private final Gson gson;
	
	public JsonRepresenter( final Gson gson )
	{
		this.gson = gson;
	}
	
	@Override
	public JsonRepresenter<S> create()
	{
		return new JsonRepresenter<>( gson );
	}

	@Override
	public JsonRepresentation<S> encode( final S s )
	{
		return new JsonRepresentation<>( gson.toJsonTree( s ) );
	}
}
