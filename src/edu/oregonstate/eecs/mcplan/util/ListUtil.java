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

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public class ListUtil
{
	public static <T> void populateList( final List<T> list, final T element, final int n )
	{
		for( int i = 0; i < n; ++i ) {
			list.add( element );
		}
	}
	
	public static <T> String join( final T[] tokens, final String sep )
	{
		if( tokens.length == 0 ) {
			return "";
		}
		final StringBuilder sb = new StringBuilder( tokens[0].toString() );
		for( int i = 1; i < tokens.length; ++i ) {
			sb.append( sep ).append( tokens[i] );
		}
		return sb.toString();
	}
	
	public static <T> String join( final int[] tokens, final String sep )
	{
		if( tokens.length == 0 ) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		sb.append( tokens[0] );
		for( int i = 1; i < tokens.length; ++i ) {
			sb.append( sep ).append( tokens[i] );
		}
		return sb.toString();
	}
	
	// -----------------------------------------------------------------------
	
	public static <T> void randomShuffle( final RandomGenerator rng, final List<T> v )
	{
		for( int i = v.size() - 1; i >= 0; --i ) {
			final int idx = rng.nextInt( i + 1 );
			final T t = v.get( idx );
			v.set( idx, v.get( i ) );
			v.set( i, t );
		}
	}
	
	public static <T> void randomShuffle( final RandomGenerator rng, final T[] v )
	{
		for( int i = v.length - 1; i >= 0; --i ) {
			final int idx = rng.nextInt( i + 1 );
			final T t = v[idx];
			v[idx] = v[i];
			v[i] = t;
		}
	}
	
	public static void randomShuffle( final RandomGenerator rng, final int[] v )
	{
		for( int i = v.length - 1; i >= 0; --i ) {
			final int idx = rng.nextInt( i + 1 );
			final int t = v[idx];
			v[idx] = v[i];
			v[i] = t;
		}
	}
}
