/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

/**
 * @author jhostetler
 *
 */
public class Tuple
{
	public static final class Tuple2<A, B>
	{
		public static <A, B> Tuple2<A, B> of( final A a, final B b )
		{
			return new Tuple2<A, B>( a, b );
		}
		
		public final A _1;
		public final B _2;
		
		public Tuple2( final A _1, final B _2 )
		{
			this._1 = _1;
			this._2 = _2;
		}
		
		@Override
		public int hashCode()
		{
			return 499 * _1.hashCode() + _2.hashCode();
		}
		
		@Override
		public boolean equals( final Object obj )
		{
			if( obj == null || !(obj instanceof Tuple2) ) {
				return false;
			}
			
			final Tuple2<?, ?> that = (Tuple2<?, ?>) obj;
			return that._1.equals( _1 ) && that._2.equals( _2 );
		}
		
		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder();
			sb.append( "(" ).append( (_1 == null ? "null" :_1.toString()) )
			  .append( ", " ).append( (_2 == null ? "null" : _2.toString()) ).append( ")" );
			return sb.toString();
		}
	}
}
