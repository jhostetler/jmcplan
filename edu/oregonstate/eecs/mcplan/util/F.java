/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import java.util.List;
import java.util.ListIterator;

/**
 * F for "functional".
 * 
 * @author jhostetler
 */
public final class F
{
	public static interface DoubleSlice
	{
		public abstract boolean hasNext();
		public abstract double next();
	}
	
	public static final class DoubleArraySlice implements DoubleSlice
	{
		private final double[] v_;
		private final int start_;
		private final int end_;
		private int i_;
		
		public DoubleArraySlice( final double[] v, final int start, final int end )
		{
			v_ = v;
			start_ = start;
			end_ = end;
			i_ = start;
		}
		
		@Override
		public boolean hasNext()
		{
			return i_ < end_;
		}
		
		@Override
		public double next()
		{
			return v_[i_++];
		}
	}
	
	public static final class DoubleListSlice implements DoubleSlice
	{
		private final ListIterator<Double> itr_;
		private final int start_;
		private final int end_;
		private int i_;
		
		public DoubleListSlice( final List<Double> v, final int start, final int end )
		{
			itr_ = v.listIterator( start );
			start_ = start;
			end_ = end;
			i_ = start;
		}
		
		@Override
		public boolean hasNext()
		{
			return i_ < end_;
		}
		
		@Override
		public double next()
		{
			++i_;
			return itr_.next();
		}
	}
	
	public static interface Function1<R, T>
	{
		public abstract R apply( final T a );
	}
	
	public static interface Predicate<T> extends Function1<Boolean, T> { };
	
	public static interface PredicateInt
	{
		public abstract boolean apply( final int i );
	}
	
	public static final class Pred
	{
		public static final class EqInt implements PredicateInt
		{
			public final int i;
			public EqInt( final int i ) { this.i = i; }
			@Override
			public boolean apply( final int j ) { return j == i; }
		}
		
		public static final class GreaterInt implements PredicateInt
		{
			public final int i;
			public GreaterInt( final int i ) { this.i = i; }
			@Override
			public boolean apply( final int j ) { return j > this.i; }
		}
		
		public static final class GreaterEqInt implements PredicateInt
		{
			public final int i;
			public GreaterEqInt( final int i ) { this.i = i; }
			@Override
			public boolean apply( final int j ) { return j >= this.i; }
		}
	}
	
	// -----------------------------------------------------------------------
	// sum
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
	// all
	// -----------------------------------------------------------------------
	
	public static <T> boolean all( final Predicate<T> p, final List<T> xs )
	{
		for( final T t : xs ) {
			if( !p.apply( t ) ) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean all( final PredicateInt p, final int[] xs )
	{
		for( final int i : xs ) {
			if( !p.apply( i ) ) {
				return false;
			}
		}
		return true;
	}
	
	// -----------------------------------------------------------------------
	// all
	// -----------------------------------------------------------------------
	
	public static <T> boolean any( final Predicate<T> p, final List<T> xs )
	{
		for( final T t : xs ) {
			if( p.apply( t ) ) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean any( final PredicateInt p, final int[] xs )
	{
		for( final int i : xs ) {
			if( p.apply( i ) ) {
				return true;
			}
		}
		return false;
	}
	
	// -----------------------------------------------------------------------
	// slice
	// -----------------------------------------------------------------------
	
	public static DoubleSlice slice( final double[] v, final int start, final int end )
	{
		return new DoubleArraySlice( v, start, end );
	}
	
	public static DoubleSlice slice( final List<Double> v, final int start, final int end )
	{
		return new DoubleListSlice( v, start, end );
	}
	
	// -----------------------------------------------------------------------
	// misc
	// -----------------------------------------------------------------------
	
	/**
	 * Normalize a vector *in place*. The vector 'v' is normalized and then
	 * returned. Normalization is exact in the sense that the returned
	 * vector sums to exactly 1.0.
	 * @param v Must be length > 0
	 * @return A reference to 'v', after normalizing 'v' in place.
	 */
	public static double[] normalize( final double[] v )
	{
		// We ensure exact normalization by accumulating errors in 'r'.
		final double s = 1.0 / F.sum( v );
		double r = 1.0;
		for( int i = 0; i < v.length - 1; ++i ) {
			v[i] *= s;
			r -= v[i];
		}
		v[v.length - 1] = r;
		return v;
	}
	
}
