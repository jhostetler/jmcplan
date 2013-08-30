/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import gnu.trove.list.array.TDoubleArrayList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Fn for "functional".
 * 
 * @author jhostetler
 */
public final class Fn
{
	// -----------------------------------------------------------------------
	// Slice types
	// -----------------------------------------------------------------------
	
	/**
	 * An Iterator whose 'remove()' method always throws. Implements 'Iterable'
	 * so that it can be used in for-each loops.
	 * 
	 * FIXME: Making this Iterable leads to unintuitive behavior with for-each
	 * loops. Namely, if you try to traverse it twice, instead of throwing,
	 * the second one will return no elements.
	 * 
	 * @param <T>
	 */
//	public static abstract class View<T> implements Iterator<T>
//	{
//		@Override
//		public abstract boolean hasNext();
//		@Override
//		public abstract T next();
//
//		@Override
//		public final void remove() { throw new UnsupportedOperationException(); }
//
////		@Override
////		public Iterator<T> iterator() { return this; }
//	}
	
	public static final class ArraySlice<T> extends Generator<T>
	{
		private final T[] v_;
		private final int start_;
		private final int end_;
		private int i_;
		
		public ArraySlice( final T[] v )
		{
			this( v, 0, v.length );
		}
		
		public ArraySlice( final T[] v, final int start, final int end )
		{
			v_ = v;
			start_ = start;
			end_ = end;
			i_ = start;
		}
		
		@Override
		public boolean hasNext() { return i_ < end_; }
		
		@Override
		public T next() { return v_[i_++]; }
	}
	
	public static final class IteratorSlice<T> extends Generator<T>
	{
		private final Iterator<T> itr_;
		
		public IteratorSlice( final Iterable<T> xs )
		{
			itr_ = xs.iterator();
		}
		
		@Override
		public boolean hasNext() { return itr_.hasNext(); }
		
		@Override
		public T next() { return itr_.next(); }
	}
	
	public static final class ListSlice<T> extends Generator<T>
	{
		private final ListIterator<T> itr_;
		private final int start_;
		private final int end_;
		private int i_;
		
		public ListSlice( final List<T> xs )
		{
			this( xs, 0, xs.size() );
		}
		
		public ListSlice( final List<T> v, final int start, final int end )
		{
			itr_ = v.listIterator( start );
			start_ = start;
			end_ = end;
			i_ = start;
		}
		
		@Override
		public boolean hasNext() { return i_ < end_; }
		
		@Override
		public T next() { ++i_; return itr_.next(); }
	}
	
	// -----------------------------------------------------------------------
	
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
		public boolean hasNext() { return i_ < end_; }
		
		@Override
		public double next() { return v_[i_++]; }
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
		public boolean hasNext() { return i_ < end_; }
		
		@Override
		public double next() { ++i_; return itr_.next(); }
	}
	
	// -----------------------------------------------------------------------
	
	public static interface IntSlice
	{
		public abstract boolean hasNext();
		public abstract int next();
	}
	
	public static final class IntArraySlice implements IntSlice
	{
		private final int[] v_;
		private final int start_;
		private final int end_;
		private int i_;
		
		public IntArraySlice( final int[] v, final int start, final int end )
		{
			v_ = v;
			start_ = start;
			end_ = end;
			i_ = start;
		}
		
		@Override
		public boolean hasNext() { return i_ < end_; }
		
		@Override
		public int next() { return v_[i_++]; }
	}
	
	public static final class IntListSlice implements IntSlice
	{
		private final ListIterator<Integer> itr_;
		private final int start_;
		private final int end_;
		private int i_;
		
		public IntListSlice( final List<Integer> v, final int start, final int end )
		{
			itr_ = v.listIterator( start );
			start_ = start;
			end_ = end;
			i_ = start;
		}
		
		@Override
		public boolean hasNext() { return i_ < end_; }
		
		@Override
		public int next() { ++i_; return itr_.next(); }
	}
	
	// -----------------------------------------------------------------------
	// Function objects
	// -----------------------------------------------------------------------
	
	public static interface Function1<R, T>
	{
		public abstract R apply( final T a );
	}
	
	public static interface IntFunction1<T>
	{
		public abstract int apply( final T a );
	}
	
	public static interface DoubleFunction1<T>
	{
		public abstract double apply( final T a );
	}
	
	public static interface Function2<R, A, B>
	{
		public abstract R apply( final A a, final B b );
	}
	
	public static interface Predicate<T>
	{
		public abstract boolean apply( final T t );
	}
	
	public static interface PredicateInt
	{
		public abstract boolean apply( final int i );
	}
	
	public static interface PredicateDouble
	{
		public abstract boolean apply( final double d );
	}
	
	// -----------------------------------------------------------------------
	// Built-in functions
	// -----------------------------------------------------------------------
	
	public static final class Pred
	{
		public static final class EqIntP implements PredicateInt
		{
			public final int x;
			private EqIntP( final int x ) { this.x = x; }
			@Override
			public boolean apply( final int y ) { return y == x; }
		}
		
		public static final class EqDoubleP implements PredicateDouble
		{
			public final double x;
			private EqDoubleP( final double x ) { this.x = x; }
			@Override
			public boolean apply( final double y ) { return y == x; }
		}
		
		public static EqIntP Eq( final int x ) { return new EqIntP( x ); }
		public static EqDoubleP Eq( final double x ) { return new EqDoubleP( x ); }
		
		// -------------------------------------------------------------------
		
		public static final class GreaterIntP implements PredicateInt
		{
			public final int x;
			private GreaterIntP( final int x ) { this.x = x; }
			@Override
			public boolean apply( final int y ) { return y > x; }
		}
		
		public static final class GreaterDoubleP implements PredicateDouble
		{
			public final double x;
			private GreaterDoubleP( final double x ) { this.x = x; }
			@Override
			public boolean apply( final double y ) { return y > x; }
		}
		
		public static GreaterIntP Greater( final int x ) { return new GreaterIntP( x ); }
		public static GreaterDoubleP Greater( final double x ) { return new GreaterDoubleP( x ); }
		
		// -------------------------------------------------------------------
		
		public static final class GreaterEqIntP implements PredicateInt
		{
			public final int x;
			private GreaterEqIntP( final int x ) { this.x = x; }
			@Override
			public boolean apply( final int y ) { return y >= x; }
		}
		
		public static final class GreaterEqDoubleP implements PredicateDouble
		{
			public final double x;
			private GreaterEqDoubleP( final double x ) { this.x = x; }
			@Override
			public boolean apply( final double y ) { return y >= x; }
		}
		
		public static GreaterEqIntP GreaterEq( final int x ) { return new GreaterEqIntP( x ); }
		public static GreaterEqDoubleP GreaterEq( final double x ) { return new GreaterEqDoubleP( x ); }
		
		// -------------------------------------------------------------------
		
		public static final class NullP implements Predicate<Object>
		{
			public static NullP Instance = new NullP();
			@Override
			public boolean apply( final Object x ) { return x == null; }
		}
		
		/** Null Object predicate. */
		public static NullP Null() { return NullP.Instance; }
	}
	
	// -----------------------------------------------------------------------
	// map
	// -----------------------------------------------------------------------
	
	private static final class LazyMapSlice<S, T> extends Generator<T>
	{
		private final Function1<T, S> f_;
		private final Iterator<S> xs_;
		
		public LazyMapSlice( final Function1<T, S> f, final Iterator<S> xs )
		{ f_ = f; xs_ = xs; }
		
		@Override
		public boolean hasNext() { return xs_.hasNext(); }
		@Override
		public T next() { return f_.apply( xs_.next() ); }
	}
	
	private static final class LazyMapIntSlice<T> implements IntSlice
	{
		private final IntFunction1<T> f_;
		private final Iterator<T> xs_;
		
		public LazyMapIntSlice( final IntFunction1<T> f, final Iterator<T> xs )
		{ f_ = f; xs_ = xs; }
		
		@Override
		public boolean hasNext() { return xs_.hasNext(); }
		@Override
		public int next() { return f_.apply( xs_.next() ); }
	}
	
	private static final class LazyMapDoubleSlice<T> implements DoubleSlice
	{
		private final DoubleFunction1<T> f_;
		private final Iterator<T> xs_;
		
		public LazyMapDoubleSlice( final DoubleFunction1<T> f, final Iterator<T> xs )
		{ f_ = f; xs_ = xs; }
		
		@Override
		public boolean hasNext() { return xs_.hasNext(); }
		@Override
		public double next() { return f_.apply( xs_.next() ); }
	}
	
	public static <S, T> Generator<T> map( final Function1<T, S> f, final Generator<S> xs )
	{
		return new LazyMapSlice<S, T>( f, xs );
	}
	
	public static <S, T> Generator<T> map( final Function1<T, S> f, final Iterable<S> xs )
	{
		return new LazyMapSlice<S, T>( f, xs.iterator() );
	}
	
	public static <T> IntSlice map( final IntFunction1<T> f, final Generator<T> xs )
	{
		return new LazyMapIntSlice<T>( f, xs );
	}
	
	public static <T> DoubleSlice map( final DoubleFunction1<T> f, final Generator<T> xs )
	{
		return new LazyMapDoubleSlice<T>( f, xs );
	}
	
	// -----------------------------------------------------------------------
	// fold
	// -----------------------------------------------------------------------
	
	public static final <A, B> A foldl( final Function2<A, A, B> f, final A x, final Iterator<B> xs )
	{
		A xp = x;
		while( xs.hasNext() ) {
			xp = f.apply( xp, xs.next() );
		}
		return xp;
	}
	
	// -----------------------------------------------------------------------
	// filter
	// -----------------------------------------------------------------------
	
	private static final class LazyFilterSlice<T> extends Generator<T>
	{
		private final Predicate<T> p_;
		private final Generator<T> xs_;
		private T next_ = null;
		private boolean has_next_ = false;
		
		public LazyFilterSlice( final Predicate<T> p, final Generator<T> xs )
		{
			p_ = p;
			xs_ = xs;
			advance();
		}
		
		private void advance()
		{
			has_next_ = false;
			while( xs_.hasNext() ) {
				next_ = xs_.next();
				if( p_.apply( next_ ) ) {
					has_next_ = true;
					break;
				}
			}
		}
		
		@Override
		public boolean hasNext() { return has_next_; }
		
		@Override
		public T next()
		{
			if( next_ == null ) {
				throw new NoSuchElementException();
			}
			final T result = next_;
			advance();
			return result;
		}
	}
	
	public static <T> Generator<T> filter( final Predicate<T> p, final Generator<T> xs )
	{
		return new LazyFilterSlice<T>( p, xs );
	}
	
	// -----------------------------------------------------------------------
	// zipWith
	// -----------------------------------------------------------------------
	
	private static final class LazyZipSlice<A, B, T> extends Generator<T>
	{
		private final Function2<T, A, B> f_;
		private final Generator<A> as_;
		private final Generator<B> bs_;
		
		public LazyZipSlice( final Function2<T, A, B> f, final Generator<A> as, final Generator<B> bs )
		{ f_ = f; as_ = as; bs_ = bs; }
		
		@Override
		public boolean hasNext() { return as_.hasNext() && bs_.hasNext(); }
		@Override
		public T next() { return f_.apply( as_.next(), bs_.next() ); }
	}
	
	public static <A, B, T> Generator<T> zipWith( final Function2<T, A, B> f, final Generator<A> as, final Generator<B> bs )
	{
		return new LazyZipSlice<A, B, T>( f, as, bs );
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
	
	public static int sum( final IntSlice v )
	{
		int s = 0;
		while( v.hasNext() ) {
			s += v.next();
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
	
	/**
	 * True if all elements of the list satisfy the predicate.
	 * @param p
	 * @param xs
	 * @return
	 */
	public static <T> boolean all( final Predicate<T> p, final List<? extends T> xs )
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
	// any
	// -----------------------------------------------------------------------
	
	/**
	 * True if any element of the list satisfies the predicate.
	 * @param p
	 * @param xs
	 * @return
	 */
	public static <T> boolean any( final Predicate<T> p, final List<? extends T> xs )
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
	// keep/remove
	// -----------------------------------------------------------------------
	
	/**
	 * Discards elements whose indices are NOT in 'idx'.
	 * @param xs
	 * @param idx
	 * @return
	 */
	public static <T> Generator<T> keep( final Generator<T> xs, final int[] idx )
	{
		return Fn.removeImpl( xs, idx, true );
	}
	
	/**
	 * Discards elements whose indices are in 'idx'.
	 * @param xs
	 * @param idx
	 * @return
	 */
	public static <T> Generator<T> remove( final Generator<T> xs, final int[] idx )
	{
		return Fn.removeImpl( xs, idx, false );
	}
	
	public static <T> Generator<T> removeImpl( final Generator<T> xs, final int[] idx, final boolean b )
	{
		return Fn.filter(
			new Predicate<T>() {
				int i = 0;
				int idx_i = 0;
				
				@Override
				public boolean apply( final T t )
				{
					if( idx_i == idx.length ) {
						return false;
					}
					else if( i++ == idx[idx_i] ) {
						++idx_i;
						return b;
					}
					else {
						return !b;
					}
				}
			}, xs );
	}
	
	// -----------------------------------------------------------------------
	// repeat
	// -----------------------------------------------------------------------
	
	public static boolean[] repeat( final boolean x, final int n )
	{
		final boolean[] result = new boolean[n];
		Arrays.fill( result, x );
		return result;
	}
	
	public static int[] repeat( final int x, final int n )
	{
		final int[] result = new int[n];
		Arrays.fill( result, x );
		return result;
	}
	
	public static double[] repeat( final double x, final int n )
	{
		final double[] result = new double[n];
		Arrays.fill( result, x );
		return result;
	}
	
	public static <T> T[] repeat( final Class<T> c, final T x, final int n )
	{
		@SuppressWarnings( "unchecked" )
		final T[] result = (T[]) Array.newInstance( c, n );
		Arrays.fill( result, x );
		return result;
	}
	
	public static <T> ArrayList<T> repeat( final T x, final int n )
	{
		final ArrayList<T> result = new ArrayList<T>( n );
		for( int i = 0; i < n; ++i ) {
			result.add( x );
		}
		return result;
	}
	
	// -----------------------------------------------------------------------
	// reverse
	// -----------------------------------------------------------------------
	
	private static final class ReverseListView<T> extends Generator<T>
	{
		private final ListIterator<T> itr_;
		public ReverseListView( final List<T> list )
		{ itr_ = list.listIterator( list.size() ); }
		
		@Override
		public boolean hasNext()
		{ return itr_.hasPrevious(); }
		
		@Override
		public T next()
		{ return itr_.previous(); }
	}
	
	private static final class Reversed<T> implements Iterable<T>
	{
		private final List<T> list_;
		public Reversed( final List<T> list )
		{ list_ = list; }
		
		@Override
		public Iterator<T> iterator()
		{ return new ReverseListView<T>( list_ ); }
	}
	
	public static <T> Iterable<T> reverse( final List<T> xs )
	{
		return new Reversed<T>( xs );
	}
	
	// -----------------------------------------------------------------------
	// take
	// -----------------------------------------------------------------------
	
	/**
	 * Takes the first 'n' elements of the (potentially infinite) Generator 'xs'
	 * and returns them as a List.
	 * @param xs
	 * @param n
	 * @return
	 */
	public static <T> List<T> take( final Generator<T> xs, final int n )
	{
		final ArrayList<T> result = new ArrayList<T>( n );
		for( int i = 0; i < n; ++i ) {
			result.add( xs.next() );
		}
		return result;
	}
	
	/**
	 * Takes all elements of the Generator 'xs' and returns them as a List. Will
	 * not return if 'xs' is infinite.
	 * @param xs
	 * @param n
	 * @return
	 */
	public static <T> List<T> takeAll( final Generator<T> xs )
	{
		final ArrayList<T> result = new ArrayList<T>();
		while( xs.hasNext() ) {
			final T t = xs.next();
			result.add( t );
		}
		return result;
	}
	
	public static double[] takeAll( final DoubleSlice xs )
	{
		final TDoubleArrayList list = new TDoubleArrayList();
		while( xs.hasNext() ) {
			list.add( xs.next() );
		}
		return list.toArray();
	}
	
	// -----------------------------------------------------------------------
	// in
	// -----------------------------------------------------------------------
	
	private static class OnceIterable<T> implements Iterable<T>
	{
		private boolean used_ = false;
		private final Iterator<T> itr_;
		
		public OnceIterable( final Iterator<T> itr )
		{ itr_ = itr; }
		
		@Override
		public Iterator<T> iterator()
		{
			if( used_ ) {
				throw new IllegalStateException( "OnceIterable already invoked");
			}
			used_ = true;
			return itr_;
		}
	};
	
	/**
	 * Adapts an Iterator into an Iterable so that it can be used in a for-each
	 * loop. The returned Iterable will throw an exception if 'iterator()' is
	 * called on it more than once.
	 * <p>
	 * This will generally be called with a temporary as the final step of a
	 * functional operation, as in
	 * <code>
	 *     for( foo : Fn.in( Fn.map( ... ) ) )
	 * </code>
	 * @param itr
	 * @return
	 */
	public static <T> Iterable<T> in( final Iterator<T> itr )
	{
		return new OnceIterable<T>( itr );
	}
	
	// -----------------------------------------------------------------------
	// memcpy
	// -----------------------------------------------------------------------
	
	/**
	 * Copies 'src' element-wise into 'dest' and returns 'dest'.
	 * @param dest
	 * @param src
	 * @param n
	 * @return
	 */
	public static int[] memcpy( final int[] dest, final int[] src, final int n )
	{
		assert( dest.length == src.length );
		for( int i = 0; i < n; ++i ) {
			dest[i] = src[i];
		}
		return dest;
	}
	
	public static <T> ArrayList<T> memcpy( final ArrayList<T> dest, final ArrayList<T> src )
	{
		assert( dest.size() == src.size() );
		for( int i = 0; i < dest.size(); ++i ) {
			dest.set( i, src.get( i ) );
		}
		return dest;
	}
	
	// -----------------------------------------------------------------------
	// linspace
	// -----------------------------------------------------------------------
	
	/**
	 * Returns an array of 'n' consecutive integers starting from 'start'.
	 * @param start
	 * @param n
	 * @return
	 */
	public static int[] linspace( final int start, final int n )
	{ return Fn.linspace( start, n, 1 ); }
	
	/**
	 * Returns an array of 'n' evenly-spaces integers starting at 'start'
	 * with increment 'step'.
	 * @param start
	 * @param n
	 * @param step
	 * @return
	 */
	public static int[] linspace( final int start, final int n, final int step )
	{
		final int[] result = new int[n];
		int x = start;
		for( int i = 0; i < n; ++i ) {
			result[i] = x;
			x += step;
		}
		return result;
	}
	
	// -----------------------------------------------------------------------
	// misc
	// -----------------------------------------------------------------------
	
	/**
	 * Returns a new vector containing a - b.
	 * @param a
	 * @param b
	 * @return
	 */
	public static int[] vminus( final int[] a, final int[] b )
	{
		assert( a.length == b.length );
		final int[] result = new int[a.length];
		for( int i = 0; i < a.length; ++i ) {
			result[i] = a[i] - b[i];
		}
		return result;
	}
	
	/**
	 * Returns a new vector containing a - b.
	 * @param a
	 * @param b
	 * @return
	 */
	public static double[] vminus( final double[] a, final double[] b )
	{
		assert( a.length == b.length );
		final double[] result = new double[a.length];
		for( int i = 0; i < a.length; ++i ) {
			result[i] = a[i] - b[i];
		}
		return result;
	}
	
	/**
	 * 'a' is modified in-place by subtracting 'b' element-wise.
	 * @param a
	 * @param b
	 * @return
	 */
	public static int[] vminus_inplace( final int[] a, final int[] b )
	{
		for( int i = 0; i < a.length; ++i ) {
			a[i] -= b[i];
		}
		return a;
	}
	
	/**
	 * 'a' is modified in-place by adding 'b' element-wise.
	 * @param a
	 * @param b
	 * @return
	 */
	public static int[] vplus_inplace( final int[] a, final int[] b )
	{
		for( int i = 0; i < a.length; ++i ) {
			a[i] += b[i];
		}
		return a;
	}

	/**
	 * Normalize a vector *in place*. The vector 'v' is normalized and then
	 * returned. Normalization is exact in the sense that the returned
	 * vector sums to exactly 1.0.
	 * @param v Must be length > 0
	 * @return A reference to 'v', after normalizing 'v' in place.
	 */
	public static double[] normalize_inplace( final double[] v )
	{
		// We ensure exact normalization by accumulating errors in 'r'.
		final double s = 1.0 / Fn.sum( v );
		double r = 1.0;
		for( int i = 0; i < v.length - 1; ++i ) {
			v[i] *= s;
			r -= v[i];
		}
		v[v.length - 1] = r;
		return v;
	}
	
	// Tests
	
	public static void main( final String[] args )
	{
		final List<Integer> xs = new ArrayList<Integer>();
		for( int i = 0; i < 10; ++i ) { xs.add( i ); }
		final int[] drop = new int[] { 1, 3, 5, 7, 9 };
		System.out.println( xs );
		for( final Integer i : Fn.takeAll( Fn.remove( new Fn.IteratorSlice<Integer>( xs ), drop ) ) ) {
			System.out.print( " " + i );
		}
		for( final Integer i : Fn.takeAll( Fn.keep( new Fn.IteratorSlice<Integer>( xs ), drop ) ) ) {
			System.out.print( " " + i );
		}
	}
	
}
