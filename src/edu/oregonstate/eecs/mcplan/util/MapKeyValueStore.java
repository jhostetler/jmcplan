/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import java.util.List;
import java.util.Map;

/**
 * @author jhostetler
 *
 */
public class MapKeyValueStore implements KeyValueStore
{
	private final List<String> keys_;
	private final Map<String, String> m_;
	
	public MapKeyValueStore( final List<String> keys, final Map<String, String> m )
	{
		keys_ = keys;
		m_ = m;
	}
	
	@Override
	public String get( final String key )
	{
		final String value = m_.get( key );
		if( value == null ) {
			throw new IllegalArgumentException( "No mapping for '" + key + "'" );
		}
		return value;
	}
	
	@Override
	public boolean getBoolean( final String key )
	{
		return Boolean.parseBoolean( get( key ) );
	}
	
	@Override
	public double getDouble( final String key )
	{
		return Double.parseDouble( get( key ) );
	}

	@Override
	public int getInt( final String key )
	{
		return Integer.parseInt( get( key ) );
	}

	@Override
	public Iterable<String> keys()
	{
		return keys_;
	}
}
