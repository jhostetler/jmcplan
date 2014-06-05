/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author jhostetler
 *
 */
public class CsvConfigurationParser implements Iterable<KeyValueStore>
{
	public final ArrayList<String> headers = new ArrayList<String>();
	private final ArrayList<Map<String, String>> data_ = new ArrayList<Map<String, String>>();
	
	public CsvConfigurationParser( final Reader in ) throws IOException
	{
		final BufferedReader reader = new BufferedReader( in );
		while( true ) {
			final String line = reader.readLine();
			if( line != null ) {
				if( headers.isEmpty() ) {
					createHeaders( line );
				}
				else {
					data_.add( createHashMap( line ) );
				}
			}
			else {
				break;
			}
		}
	}
	
	@Override
	public Iterator<KeyValueStore> iterator()
	{
		return new Itr( data_.iterator() );
	}
	
	public KeyValueStore get( final int i )
	{
		return new MapKeyValueStore( headers, data_.get( i ) );
	}
	
	public int size()
	{
		return data_.size();
	}
	
	private void createHeaders( final String line )
	{
		final String[] h = line.split( "," );
		for( int i = 0; i < h.length; ++i ) {
			headers.add( h[i] );
		}
	}
	
	private Map<String, String> createHashMap( final String line )
	{
		final String[] cells = line.split( "," );
		assert( cells.length == headers.size() );
		final HashMap<String, String> m = new HashMap<String, String>();
		for( int i = 0; i < cells.length; ++i ) {
			m.put( headers.get( i ), cells[i] );
		}
		return m;
	}
	
	private class Itr implements Iterator<KeyValueStore>
	{
		private final Iterator<Map<String, String>> itr_;
		public Itr( final Iterator<Map<String, String>> itr )
		{ itr_ = itr; }
		
		@Override
		public boolean hasNext()
		{
			return itr_.hasNext();
		}

		@Override
		public KeyValueStore next()
		{
			final Map<String, String> m = itr_.next();
			return new MapKeyValueStore( headers, m );
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}
