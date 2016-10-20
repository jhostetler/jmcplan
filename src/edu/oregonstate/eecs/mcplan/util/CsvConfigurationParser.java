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
