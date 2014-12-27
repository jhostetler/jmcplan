/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

/**
 * @author jhostetler
 *
 */
public class PreferredNumbers
{
	/**
	 * Returns a generator of the infinte series '1 2 5 10 20 50 100 ...'
	 * @return
	 */
	public static Fn.IntSlice Series_1_2_5()
	{
		return new Fn.IntSlice() {
			int n = 1;
			int scale = 1;
			
			@Override
			public boolean hasNext()
			{ return true; }

			@Override
			public int next()
			{
				final int next = n*scale;
				switch( n ) {
					case 1: n = 2; break;
					case 2: n = 5; break;
					case 5: n = 1; scale *= 10; break;
					default: throw new AssertionError( "unreachable" );
				}
				return next;
			}
			
		};
	}
}
