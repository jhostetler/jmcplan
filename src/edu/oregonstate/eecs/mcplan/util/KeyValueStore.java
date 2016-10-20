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

/**
 * @author jhostetler
 *
 */
public abstract class KeyValueStore
{
	public abstract Iterable<String> keys();
	public abstract String get( final String key );
	
	public String defaultValue( final String key )
	{
		throw new UnsupportedOperationException();
	}
	
	// -----------------------------------------------------------------------
	
	public final boolean getBoolean( final String key )
	{
		final String value = get( key );
		if( value == null ) {
			return parseBoolean( defaultValue( key ) );
		}
		else {
			return parseBoolean( value );
		}
	}
	
	public final double getDouble( final String key )
	{
		final String value = get( key );
		if( value == null ) {
			return parseDouble( defaultValue( key ) );
		}
		else {
			return parseDouble( value );
		}
	}
	
	public final int getInt( final String key )
	{
		final String value = get( key );
		if( value == null ) {
			return parseInt( defaultValue( key ) );
		}
		else {
			return parseInt( value );
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final boolean parseBoolean( final String s )
	{
		if( "true".equalsIgnoreCase( s ) ) {
			return true;
		}
		else if( "false".equalsIgnoreCase( s ) ) {
			return false;
		}
		else {
			throw new IllegalArgumentException( s );
		}
	}
	
	private final double parseDouble( final String s )
	{
		return Double.parseDouble( s );
	}
	
	private final int parseInt( final String s )
	{
		return Integer.parseInt( s );
	}
}
