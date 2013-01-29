/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import java.util.List;

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
}
