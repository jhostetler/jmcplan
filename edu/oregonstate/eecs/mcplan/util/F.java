/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import java.util.Arrays;

/**
 * F for "functional".
 * 
 * @author jhostetler
 */
public final class F
{
	public static final class DoubleSlice
	{
		private final double[] v_;
		private final int start_;
		private final int end_;
		private int i_;
		
		public DoubleSlice( final double[] v, final int start, final int end )
		{
			v_ = v;
			start_ = start;
			end_ = end;
			i_ = start;
		}
		
		public boolean hasNext()
		{
			return i_ < end_;
		}
		
		public double next()
		{
			return v_[i_++];
		}
		
		public double[] toArray()
		{
			return Arrays.copyOfRange( v_, start_, end_ );
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static int sum( final int[] v )
	{
		int s = 0;
		for( final int i : v ) {
			s += i;
		}
		return s;
	}
	
	public static double sum( final double[] v )
	{
		double s = 0.0;
		for( final double d : v ) {
			s += d;
		}
		return s;
	}
	
	public static double sum( final DoubleSlice v )
	{
		double s = 0.0;
		while( v.hasNext() ) {
			s += v.next();
		}
		return s;
	}
	
	// -----------------------------------------------------------------------
	
	public static DoubleSlice slice( final double[] v, final int start, final int end )
	{
		return new DoubleSlice( v, start, end );
	}
}
