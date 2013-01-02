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
}
