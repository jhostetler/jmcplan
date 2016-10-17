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
public class MapKeyValueStore extends KeyValueStore
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
		return m_.get( key );
	}

	@Override
	public Iterable<String> keys()
	{
		return keys_;
	}
}
