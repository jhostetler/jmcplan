/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

/**
 * @author jhostetler
 *
 */
public class ConfigurationParser extends KeyValueStore
{
	private final HashMap<String, String> options_ = new HashMap<String, String>();
	
	public ConfigurationParser( final Reader in ) throws IOException
	{
		final BufferedReader reader = new BufferedReader( in );
		while( true ) {
			final String line = reader.readLine();
			if( line != null ) {
				final String trimmed = line.trim();
				if( trimmed.isEmpty() || trimmed.startsWith( "#" ) ) {
					// Comment
					continue;
				}
				final String[] kp = trimmed.split( "\\s*=\\s*" );
				if( kp.length != 2 ) {
					throw new IllegalArgumentException( line );
				}
				options_.put( kp[0], kp[1] );
			}
			else {
				break;
			}
		}
	}
	
	@Override
	public String get( final String key )
	{
		return options_.get( key );
	}

	@Override
	public Iterable<String> keys()
	{
		return options_.keySet();
	}
}
