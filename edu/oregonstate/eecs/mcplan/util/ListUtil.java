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
	
	public static String join( final String[] tokens, final String sep )
	{
		if( tokens.length == 0 ) {
			return "";
		}
		final StringBuilder sb = new StringBuilder( tokens[0] );
		for( int i = 1; i < tokens.length; ++i ) {
			sb.append( sep ).append( tokens[i] );
		}
		return sb.toString();
	}
	
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
}
