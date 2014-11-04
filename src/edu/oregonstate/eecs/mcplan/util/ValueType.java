/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import java.util.Arrays;

/**
 * @author jhostetler
 *
 */
public class ValueType<T>
{
	public static final ValueType<int[]> of( final int[] t )
	{
		return new ValueType<int[]>( t ) {
			@Override
			public boolean equals( final Object obj )
			{
				@SuppressWarnings( "unchecked" )
				final ValueType<int[]> that = (ValueType<int[]>) obj;
				return Arrays.equals( this.t, that.t );
			}
			
			@Override
			public int hashCode()
			{
				return Arrays.hashCode( this.t );
			}
			
			@Override
			public String toString()
			{
				return Arrays.toString( this.t );
			}
		};
	}
	
	// -----------------------------------------------------------------------
	
	protected final T t;
	
	public ValueType( final T t )
	{
		this.t = t;
	}
	
	public T get()
	{
		return t;
	}
}
