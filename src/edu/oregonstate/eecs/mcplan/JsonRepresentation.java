/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import com.google.gson.JsonElement;

/**
 * @author jhostetler
 *
 */
public class JsonRepresentation<S> extends Representation<S>
{
	public final JsonElement json;
	
	public JsonRepresentation( final JsonElement json )
	{
		this.json = json;
	}
	
	@Override
	public Representation<S> copy()
	{
		return new JsonRepresentation<>( json );
	}

	@Override
	public boolean equals( final Object obj )
	{
		@SuppressWarnings( "unchecked" )
		final JsonRepresentation<S> that = (JsonRepresentation<S>) obj;
		return json.equals( that.json );
	}

	@Override
	public int hashCode()
	{
		return json.hashCode();
	}
	
	@Override
	public String toString()
	{
		return json.toString();
	}

}
