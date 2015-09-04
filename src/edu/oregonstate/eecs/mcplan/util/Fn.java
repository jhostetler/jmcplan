/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.ArithmeticUtils;

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
	
	public static boolean isPowerOf2( final int x )
	{
		return x > 0 && (x & (x-1)) == 0;
	}
	
	public static boolean isPerfectSquare( final int x )
	{
		// Note: There are faster ways
		
		final int quick = x & 0xf;
		if( quick != 0 && quick != 1 && quick != 4 && quick != 9 ) {
			return false;
		}
		
		// Add 0.5 to prevent rounding error from pushing sqrt() below the
		// previous integer.
		final int test = (int) (Math.sqrt( x ) + 0.5);
		return test*test == x;
	}
	
	// -----------------------------------------------------------------------
	// Sequences
	// -----------------------------------------------------------------------
	
	private static class PositiveIntegers implements IntSlice
	{
		private int i = 1;
		
		@Override
		public boolean hasNext()
		{ return true; }

		@Override
		public int next()
		{ return i++; }
	}
	
	public static IntSlice PositiveIntegers()
	{
		return new PositiveIntegers();
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
	
	public static int sum( final boolean[] v )
	{
		int s = 0;
		for( final boolean b : v ) {
			s += (b ? 1 : 0);
		}
		return s;
	}
	
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
	// derivative
	// -----------------------------------------------------------------------
	
	public static double[] derivative( final double[] xs )
	{
		assert( xs.length > 0 );
		if( xs.length == 1 ) {
			return new double[] { 0.0 };
		}
		else {
			final double[] d = new double[xs.length - 1];
			double x = xs[0];
			for( int i = 1; i < xs.length; ++i ) {
				final double y = xs[i];
				d[i-1] = y - x;
				x = y;
			}
			return d;
		}
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
	
	public static boolean all( final boolean[] xs )
	{
		for( final boolean b : xs ) {
			if( !b ) {
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
	// min
	// -----------------------------------------------------------------------
	
	/**
	 * True if any element of the list satisfies the predicate.
	 * @param p
	 * @param xs
	 * @return
	 */
	public static int min( final int... x )
	{
		int m = Integer.MAX_VALUE;
		for( final int xi : x ) {
			if( xi < m ) {
				m = xi;
			}
		}
		return m;
	}
	
	public static int min( final TIntCollection c )
	{
		int m = Integer.MAX_VALUE;
		final TIntIterator itr = c.iterator();
		while( itr.hasNext() ) {
			final int candidate = itr.next();
			if( candidate < m ) {
				m = candidate;
			}
		}
		return m;
	}
	
	// -----------------------------------------------------------------------
	// approximate equality
	// -----------------------------------------------------------------------
	
	public static boolean approxEq( final double eps, final double x, final double y )
	{
		return Math.abs( x - y ) < eps;
	}
	
	public static boolean approxEq( final double eps, final double... xs )
	{
		assert( xs.length > 1 );
		final double comp = xs[0];
		for( int i = 1; i < xs.length; ++i ) {
			final double d = Math.abs( comp - xs[i] );
			if( d >= eps ) {
				return false;
			}
		}
		return true;
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
	
	public static double[] append( final double[] a, final double x )
	{
		final double[] aprime = new double[a.length + 1];
		Fn.memcpy( aprime, a, a.length );
		aprime[aprime.length - 1] = x;
		return aprime;
	}
	
	public static int[] append( final int[] a, final int i )
	{
		final int[] aprime = new int[a.length + 1];
		Fn.memcpy( aprime, a, a.length );
		aprime[aprime.length - 1] = i;
		return aprime;
	}
	
	public static int greatestLowerBound( final IntSlice xs, final int x )
	{
		int i = xs.next();
		while( xs.hasNext() ) {
			final int j = xs.next();
			if( j > x ) {
				break;
			}
			i = j;
		}
		return i;
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
	// range
	// -----------------------------------------------------------------------
	
	public static int[] range( final int start, final int end )
	{
		assert( end >= start );
		final int[] r = new int[end - start];
		for( int i = start; i < end; ++i ) {
			r[i - start] = i;
		}
		return r;
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
	
	public static <T> Iterable<T> reversed( final List<T> xs )
	{
		return new Reversed<T>( xs );
	}
	
	private static final class ReverseIntArrayView implements IntSlice
	{
		private final int[] a_;
		private int i = 0;
		public ReverseIntArrayView( final int[] a )
		{
			a_ = a;
		}
		@Override
		public boolean hasNext()
		{
			return i < a_.length;
		}
		@Override
		public int next()
		{
			return a_[i++];
		}
	}
	
	public static IntSlice reversed( final int[] a )
	{
		return new ReverseIntArrayView( a );
	}
	
	// -----------------------------------------------------------------------
	// shuffle
	// -----------------------------------------------------------------------
	
	/**
	 * Fisher-Yates shuffle.
	 * @param rng
	 * @param a
	 */
	public static void shuffle( final RandomGenerator rng, final boolean[] a )
	{
		shuffle( rng, a, a.length );
	}
	
	/**
	 * Fisher-Yates shuffle.
	 * @param rng
	 * @param a
	 */
	public static void shuffle( final RandomGenerator rng, final int[] a )
	{
		shuffle( rng, a, a.length );
	}
	
	/**
	 * Fisher-Yates shuffle. This version does only 'n' swaps, so the first
	 * 'n' elements of the array will be randomly selected from among the
	 * entire array.
	 * @param rng
	 * @param a
	 */
	public static void shuffle( final RandomGenerator rng, final boolean[] a, final int n )
	{
		for( int i = 0; i < n; ++i ) {
			final int j = i + rng.nextInt( n - i );
			final boolean temp = a[j];
			a[j] = a[i];
			a[i] = temp;
		}
	}
	
	/**
	 * Fisher-Yates shuffle. This version does only 'n' swaps, so the first
	 * 'n' elements of the array will be randomly selected from among the
	 * entire array.
	 * @param rng
	 * @param a
	 */
	public static void shuffle( final RandomGenerator rng, final int[] a, final int n )
	{
		for( int i = 0; i < n; ++i ) {
			final int j = i + rng.nextInt( n - i );
			final int temp = a[j];
			a[j] = a[i];
			a[i] = temp;
		}
	}
	
	/**
	 * Fisher-Yates shuffle.
	 * @param rng
	 * @param a
	 */
	public static <T> void shuffle( final RandomGenerator rng, final ArrayList<T> a )
	{
		shuffle( rng, a, a.size() );
	}
	
	/**
	 * Fisher-Yates shuffle. This version does only 'n' swaps, so the first
	 * 'n' elements of the array will be randomly selected from among the
	 * entire array.
	 * @param rng
	 * @param a
	 */
	public static <T> void shuffle( final RandomGenerator rng, final ArrayList<T> a, final int n )
	{
		for( int i = 0; i < n; ++i ) {
			final int j = i + rng.nextInt( n - i );
			final T temp = a.get( j );
			a.set( j,  a.get( i ) );
			a.set( i, temp );
		}
	}
	
	/**
	 * Fisher-Yates shuffle.
	 * @param rng
	 * @param a
	 */
	public static void shuffle( final RandomGenerator rng, final TIntList a )
	{
		shuffle( rng, a, a.size() );
	}
	
	/**
	 * Fisher-Yates shuffle. This version does only 'n' swaps, so the first
	 * 'n' elements of the array will be randomly selected from among the
	 * entire array.
	 * @param rng
	 * @param a
	 */
	public static void shuffle( final RandomGenerator rng, final TIntList a, final int n )
	{
		for( int i = 0; i < n; ++i ) {
			final int j = i + rng.nextInt( n - i );
			final int temp = a.get( j );
			a.set( j,  a.get( i ) );
			a.set( i, temp );
		}
	}
	
	/**
	 * Choose an element uniformly at random from a stream of unknown length.
	 * 
	 * Uses the "reservoir sampling" algorithm:
	 * https://en.wikipedia.org/wiki/Reservoir_sampling
	 * 
	 * @param rng
	 * @param g
	 * @return
	 */
	public static <T> T uniform_choice( final RandomGenerator rng, final Generator<T> g )
	{
		T choice = g.next();
		int i = 1;
		while( g.hasNext() ) {
			++i;
			final T t = g.next();
			if( rng.nextInt( i ) == 0 ) {
				choice = t;
			}
		}
		return choice;
	}
	
	public static <T> T uniform_choice( final RandomGenerator rng, final Iterable<T> xs )
	{
		final Iterator<T> g = xs.iterator();
		T choice = g.next();
		int i = 1;
		while( g.hasNext() ) {
			++i;
			final T t = g.next();
			if( rng.nextInt( i ) == 0 ) {
				choice = t;
			}
		}
		return choice;
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
	public static <T> ArrayList<T> takeAll( final Iterator<T> xs )
	{
		final ArrayList<T> result = new ArrayList<T>();
		while( xs.hasNext() ) {
			final T t = xs.next();
			result.add( t );
		}
		return result;
	}
	
	public static <T> ArrayList<T> takeAll( final Iterable<T> xs )
	{
		final ArrayList<T> result = new ArrayList<T>();
		for( final T t : xs ) {
			result.add( t );
		}
		return result;
	}
	
	public static <T> ArrayList<T> takeAll( final Enumeration<T> xs )
	{
		final ArrayList<T> result = new ArrayList<T>();
		while( xs.hasMoreElements() ) {
			final T t = xs.nextElement();
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
	
	private static class ArrayIterable<T> implements Iterable<T>
	{
		private final T[] array;
		
		public ArrayIterable( final T[] array )
		{
			this.array = array;
		}

		@Override
		public Iterator<T> iterator()
		{
			return new ArraySlice<T>( array );
		}
	}
	
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
	
	public static <T> Iterable<T> in( final T[] array )
	{
		return new ArrayIterable<T>( array );
	}
	
	public static <T> Iterable<Integer> in( final IntSlice itr )
	{
		return new OnceIterable<Integer>( new Generator<Integer>() {
			@Override
			public boolean hasNext()
			{ return itr.hasNext(); }

			@Override
			public Integer next()
			{ return itr.next(); }
		} );
	}
	
	public static <T> T element( final Iterable<T> iterable, final int index )
	{
		assert( index >= 0 );
		final Iterator<T> itr = iterable.iterator();
		for( int i = 0; i < index; ++i ) {
			if( !itr.hasNext() ) {
				throw new IndexOutOfBoundsException();
			}
			itr.next();
		}
		if( !itr.hasNext() ) {
			throw new IndexOutOfBoundsException();
		}
		return itr.next();
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
	public static double[] memcpy( final double[] dest, final double[] src, final int n )
	{
		assert( dest.length >= n );
		assert( src.length >= n );
		for( int i = 0; i < n; ++i ) {
			dest[i] = src[i];
		}
		return dest;
	}
	
	public static double[] memcpy_as_double( final double[] dest, final int[] src, final int n )
	{
		assert( dest.length >= n );
		assert( src.length >= n );
		for( int i = 0; i < n; ++i ) {
			dest[i] = src[i];
		}
		return dest;
	}
	
	public static double[] vcopy_as_double( final int[] src )
	{
		final double[] r = new double[src.length];
		memcpy_as_double( r, src, src.length );
		return r;
	}
	
	/**
	 * Copies 'src' element-wise into 'dest' and returns 'dest'.
	 * @param dest
	 * @param src
	 * @param n
	 * @return
	 */
	public static int[] memcpy_as_int( final int[] dest, final double[] src, final int n )
	{
		assert( dest.length >= n );
		assert( src.length >= n );
		for( int i = 0; i < n; ++i ) {
			dest[i] = (int) src[i];
		}
		return dest;
	}
	
	public static int[] vcopy_as_int( final double[] src )
	{
		final int[] r = new int[src.length];
		memcpy_as_int( r, src, src.length );
		return r;
	}
	
	/**
	 * Copies 'src' element-wise into 'dest' and returns 'dest'.
	 * @param dest
	 * @param src
	 * @param n
	 * @return
	 */
	public static int[] memcpy( final int[] dest, final int[] src, final int n )
	{
		assert( dest.length >= n );
		assert( src.length >= n );
		for( int i = 0; i < n; ++i ) {
			dest[i] = src[i];
		}
		return dest;
	}
	
	/**
	 * Copies 'src' element-wise into 'dest' and returns 'dest'.
	 * @param dest
	 * @param src
	 * @return
	 */
	public static int[] memcpy( final int[] dest, final int[] src )
	{
		return memcpy( dest, src, dest.length );
	}
	
	/**
	 * Copies 'src' element-wise into 'dest' and returns 'dest'.
	 * @param dest
	 * @param src
	 * @param n
	 * @return
	 */
	public static boolean[] memcpy( final boolean[] dest, final boolean[] src, final int n )
	{
		assert( dest.length >= n );
		assert( src.length >= n );
		for( int i = 0; i < n; ++i ) {
			dest[i] = src[i];
		}
		return dest;
	}
	
	/**
	 * Copies 'src' element-wise into 'dest' and returns 'dest'.
	 * @param dest
	 * @param src
	 * @return
	 */
	public static boolean[] memcpy( final boolean[] dest, final boolean[] src )
	{
		return memcpy( dest, src, dest.length );
	}
	
	public static byte[] memcpy( final byte[] dest, final byte[] src, final int n )
	{
		assert( dest.length >= n );
		assert( src.length >= n );
		for( int i = 0; i < n; ++i ) {
			dest[i] = src[i];
		}
		return dest;
	}
	
	public static byte[] memcpy( final byte[] dest, final byte[] src )
	{
		return memcpy( dest, src, dest.length );
	}
	
	public static <T> ArrayList<T> memcpy( final ArrayList<T> dest, final ArrayList<T> src )
	{
		assert( dest.size() == src.size() );
		for( int i = 0; i < dest.size(); ++i ) {
			dest.set( i, src.get( i ) );
		}
		return dest;
	}
	
	/**
	 * @deprecated This function can be called accidentally if you try to use
	 * a higher-dimensional memcpy that is not implemented.
	 * @param dest
	 * @param src
	 * @return
	 */
	@Deprecated
	public static <T> T[] memcpy( final T[] dest, final T[] src )
	{
		assert( dest.length == src.length );
		for( int i = 0; i < dest.length; ++i ) {
			dest[i] = src[i];
		}
		return dest;
	}
	
	public static byte[][] memcpy( final byte[][] dest, final byte[][] src )
	{
		for( int i = 0; i < dest.length; ++i ) {
			for( int j = 0; j < dest[i].length; ++j ) {
				dest[i][j] = src[i][j];
			}
		}
		return dest;
	}
	
	public static short[][] memcpy( final short[][] dest, final short[][] src )
	{
		for( int i = 0; i < dest.length; ++i ) {
			for( int j = 0; j < dest[i].length; ++j ) {
				dest[i][j] = src[i][j];
			}
		}
		return dest;
	}
	
	public static int[][] memcpy( final int[][] dest, final int[][] src )
	{
		for( int i = 0; i < dest.length; ++i ) {
			for( int j = 0; j < dest[i].length; ++j ) {
				dest[i][j] = src[i][j];
			}
		}
		return dest;
	}
	
	// Copy
	
	public static <T> ArrayList<T> copy( final ArrayList<T> x )
	{
		final ArrayList<T> c = new ArrayList<T>( x.size() );
		c.addAll( x );
		return c;
	}
	
	public static boolean[] copy( final boolean[] x )
	{
		return Arrays.copyOf( x, x.length );
	}
	
	public static byte[] copy( final byte[] x )
	{
		return Arrays.copyOf( x, x.length );
	}
	
	public static char[] copy( final char[] x )
	{
		return Arrays.copyOf( x, x.length );
	}
	
	public static int[] copy( final int[] x )
	{
		return Arrays.copyOf( x, x.length );
	}
	
	public static double[] copyAsDouble( final int[] x )
	{
		final double[] c = new double[x.length];
		for( int i = 0; i < x.length; ++i ) {
			c[i] = x[i];
		}
		return c;
	}
	
	public static double[] copy( final double[] x )
	{
		return Arrays.copyOf( x, x.length );
	}
	
	public static boolean[][] copy( final boolean[][] a )
	{
		final boolean[][] r = new boolean[a.length][];
		for( int i = 0; i < a.length; ++i ) {
			r[i] = Arrays.copyOf( a[i], a[i].length );
		}
		return r;
	}
	
	public static byte[][] copy( final byte[][] a )
	{
		final byte[][] r = new byte[a.length][];
		for( int i = 0; i < a.length; ++i ) {
			r[i] = Arrays.copyOf( a[i], a[i].length );
		}
		return r;
	}
	
	public static int[][] copy( final int[][] a )
	{
		final int[][] r = new int[a.length][];
		for( int i = 0; i < a.length; ++i ) {
			r[i] = Arrays.copyOf( a[i], a[i].length );
		}
		return r;
	}
	
	public static int[][][] copy( final int[][][] a )
	{
		final int[][][] r = new int[a.length][][];
		for( int i = 0; i < a.length; ++i ) {
			for( int j = 0; j < a[i].length; ++j ) {
				r[i][j] = Arrays.copyOf( a[i][j], a[i][j].length );
			}
		}
		return r;
	}
	
	public static void assign( final byte[] xs, final byte x )
	{
		for( int i = 0; i < xs.length; ++i ) {
			xs[i] = x;
		}
	}
	
	public static void assign( final int[] xs, final int x )
	{
		for( int i = 0; i < xs.length; ++i ) {
			xs[i] = x;
		}
	}
	
	public static void assign( final int[][] xs, final int x )
	{
		for( int i = 0; i < xs.length; ++i ) {
			final int[] xsi = xs[i];
			for( int j = 0; j < xsi.length; ++j ) {
				xsi[j] = x;
			}
		}
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
	// contains
	// -----------------------------------------------------------------------
	
	public static boolean contains( final int[] xs, final int x )
	{
		for( final int i : xs ) {
			if( i == x ) {
				return true;
			}
		}
		return false;
	}
	
	// -----------------------------------------------------------------------
	// min/max
	// -----------------------------------------------------------------------
	
	public static int argmin( final int... v )
	{
		assert( v.length > 0 );
		int min = Integer.MAX_VALUE;
		int min_idx = 0;
		for( int i = 0; i < v.length; ++i ) {
			if( v[i] < min ) {
				min = v[i];
				min_idx = i;
			}
		}
		return min_idx;
	}
	
	public static int argmax( final double... v )
	{
		assert( v.length > 0 );
		double max = -Double.MAX_VALUE;
		int max_idx = 0;
		for( int i = 0; i < v.length; ++i ) {
			if( v[i] > max ) {
				max = v[i];
				max_idx = i;
			}
		}
		return max_idx;
	}
	
	public static int argmax( final int... v )
	{
		assert( v.length > 0 );
		int max = -Integer.MAX_VALUE;
		int max_idx = 0;
		for( int i = 0; i < v.length; ++i ) {
			if( v[i] > max ) {
				max = v[i];
				max_idx = i;
			}
		}
		return max_idx;
	}
	
	public static int argmax( final IntSlice v )
	{
		int max = -Integer.MAX_VALUE;
		int max_idx = 0;
		int idx = 0;
		while( v.hasNext() ) {
			final int i = v.next();
			if( i > max ) {
				max = i;
				max_idx = idx;
			}
			idx += 1;
		}
		return max_idx;
	}
	
	public static double max( final double... v )
	{
		return v[argmax( v )];
	}
	
	public static int max( final int... v )
	{
		return v[argmax( v )];
	}
	
	/**
	 * Returns the n mod base. This is different from the Java operator% as
	 * -1 % 3 == -1, but -1 mod 3 == 2.
	 * @param n
	 * @param base
	 * @return
	 */
	public static int mod( final int n, final int base )
	{
		final int r = n % base;
		return (r < 0 ? r + base : r);
	}
	
	// -----------------------------------------------------------------------
	// Multinomial
	// -----------------------------------------------------------------------
	
	/**
	 * Computes the multinomial coefficient n multichoose k[], which is the
	 * number of ways of putting n objects into m boxes such that each box
	 * contains k_i objects.
	 * 
	 * This implementation is exact, but beware of overflow.
	 * 
	 * This is a naive implementation using the definition of the multinomial
	 * coefficient in terms of binomial coefficients. There are probably
	 * much faster ways to do this.
	 * 
	 * @param n
	 * @param k
	 * @return
	 */
	public static int multinomialCoefficient( final int n, final int[] k )
	{
		int top = k[0];
		int product = 1; // k[0] choose k[0]
		for( int i = 1; i < k.length; ++i ) {
			top += k[i];
			product *= ArithmeticUtils.binomialCoefficient( top, k[i] );
		}
		return product;
	}
	
	public static int multinomialTermCount( final int n, final int m )
	{
		return (int) ArithmeticUtils.binomialCoefficient( n + m - 1, n );
	}
	
	/**
	 * Generates all the different terms in the expansion of
	 * (x_1 + x_2 + ... + x_m)^n. Terms are represented as integer arrays
	 * representing the exponent of each variable in the term:
	 *   x_1^2 x_2^1 x_3^0 => [2, 1, 0]
	 * 
	 * The order of the terms is undefined.
	 * 
	 * Uses the "stars and bars" method. See:
	 * https://en.wikipedia.org/wiki/Stars_and_bars_%28combinatorics%29
	 */
	public static final class MultinomialTermGenerator extends Generator<int[]>
	{
		public final int n;
		public final int m;
		
		final int[] bars;
		final int[] term;
		
		private boolean done = false;
		
		public MultinomialTermGenerator( final int n, final int m )
		{
			this.n = n;
			this.m = m;
			
			bars = new int[m - 1];
			term = new int[m];
			term[0] = n;
		}

		@Override
		public boolean hasNext()
		{
			return !done;
		}
		
		@Override
		public int[] next()
		{
			assert( !done );
			
			final int[] next = Fn.copy( term );
			
			// We're implementing the "stars and bars" counting method.
			// Imagine the 5 dice are stars, and the 6 bins are represented by 5 dividing "bars":
			//    ||**|**|*| = {0, 0, 2, 2, 1, 0}
			//    *|*||*|*|* = {1, 1, 0, 1, 1, 1}
			// The algorithm moves the bars around to create different combinations
			
			int i = 0;
			for( ; i < bars.length; ++i ) {
				if( bars[i] < n ) {
					// Bar can be moved. e.g.:
					// ||**|***||
					//     ^
					bars[i] += 1;
					// => ||***|**||
					for( int j = 0; j < i; ++j ) {
						// Move all lower-order bars to the same position
						// => ||***|||**
						bars[j] = bars[i];
					}
					break;
				}
			}
			
			if( i == bars.length ) {
				done = true;
			}
			
			// Recompute totals. This could be done incrementally for greater
			// efficiency.
			term[0] = n - bars[0];
			int sum = term[0];
			for( int j = 1; j < bars.length; ++j ) {
				term[j] = bars[j - 1] - bars[j];
				sum += term[j];
			}
			term[m - 1] = n - sum;
			
			// Return state computed above
			return next;
		}
		
	}
	
	// -----------------------------------------------------------------------
	// power_set
	// -----------------------------------------------------------------------
	
//	public static int[] power_set( final int n )
//	{
//		assert( n < 32 );
//		final int N = 1 << n;
//		final int[] p = new int[N];
//		int idx = 0;
//		for( int i = 0; i < N; ++i ) {
//		    for( int j = 0; j < n; j++ ) {
//		        if( ((i>>j) & 1) == 1 ) { // bit j is on
//		            subset.add(numbers.get(j));
//		        }
//		    }
//		    // print subset
//		}
//	}
	
	public static int multisetPowerSetCardinality( final int[] M )
	{
		int r = 1;
		for( int i = 0; i < M.length; ++i ) {
			r *= (M[i] + 1);
		}
		return r;
	}
	
	public static class MultisetPowerSetGenerator extends Generator<int[]>
	{
		private final int[] M_;
		private final int[] subset_;
		private boolean changed_ = true;
		
		public MultisetPowerSetGenerator( final int[] M )
		{
			M_ = M;
			subset_ = new int[M_.length];
		}
		
		@Override
		public boolean hasNext()
		{
			return changed_;
		}

		@Override
		public int[] next()
		{
			changed_ = false;
			final int[] r = Arrays.copyOf( subset_, subset_.length );
			for( int i = 0; i < M_.length; ++i ) {
				if( subset_[i] < M_[i] ) {
					subset_[i] += 1;
					changed_ = true;
					break;
		        }
		        else {
		        	subset_[i] = 0;
		        }
		    }
			return r;
		}
		
	}
	
	
	// TODO: This is a faster power set algorithm for ordinary sets
	// Add KeepAction for each subset of dice, *except* the entire set
//	for( int i = 0; i < (1<<Hand.Ndice) - 1; ++i ) {
//		final int[] keep_idx = Fn.linspace( 0, Hand.Ndice );
//		final int[] keepers = new int[Hand.Nfaces];
//		for( int j = 0; j < Hand.Nfaces; ++j ) {
//			if( ((i>>j) & 1) == 1 ) {
//				keepers[faces[keep_idx[j]]] += 1;
//			}
//		}
//		actions_.add( new JointAction<YahtzeeAction>( new KeepAction( keepers ) ) );
//	}
	
	// -----------------------------------------------------------------------
	// misc
	// -----------------------------------------------------------------------
	
	public static double inner_product( final double[] x, final double[] y )
	{
		assert( x.length == y.length );
		double d = 0;
		for( int i = 0; i < x.length; ++i ) {
			d += x[i]*y[i];
		}
		return d;
	}
	
	public static double scalar_projection( final double[] a, final double[] b )
	{
		final double n = inner_product( a, b );
		final double d = inner_product( b, b );
		return n / d;
	}
	
	public static double[] projection( final double[] a, final double[] b )
	{
		final double n = inner_product( a, b );
		final double d = inner_product( b, b );
		return Fn.scalar_multiply( b, (n / d) );
	}
	
	public static double distance_l2( final double[] x, final double[] y )
	{
		assert( x.length == y.length );
		double d = 0.0;
		for( int i = 0; i < x.length; ++i ) {
			final double diff = x[i] - y[i];
			d += diff*diff;
		}
		return d;
	}
	
	public static double distance_l1( final double[] x, final double[] y )
	{
		assert( x.length == y.length );
		double d = 0;
		for( int i = 0; i < x.length; ++i ) {
			final double diff = Math.abs( x[i] - y[i] );
			d += diff;
		}
		return d;
	}
	
	public static int distance_l1( final int[] x, final int[] y )
	{
		assert( x.length == y.length );
		int d = 0;
		for( int i = 0; i < x.length; ++i ) {
			final int diff = Math.abs( x[i] - y[i] );
			d += diff;
		}
		return d;
	}
	
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
	
	public static double[] vabs_inplace( final double[] a )
	{
		for( int i = 0; i < a.length; ++i ) {
			a[i] = Math.abs( a[i] );
		}
		return a;
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
	 * 'a' is modified in-place by subtracting 'b' element-wise.
	 * @param a
	 * @param b
	 * @return
	 */
	public static double[] vminus_inplace( final double[] a, final double[] b )
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
	public static double[] vplus_inplace( final double[] a, final double[] b )
	{
		for( int i = 0; i < a.length; ++i ) {
			a[i] += b[i];
		}
		return a;
	}
	
	/**
	 * 'v' is modified in-place by adding 'a*x' element-wise.
	 * @param v
	 * @param a
	 * @param x
	 * @return
	 */
	public static double[] vplus_ax_inplace( final double[] v, final double a, final double[] x  )
	{
		for( int i = 0; i < v.length; ++i ) {
			v[i] += a*x[i];
		}
		return v;
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
	
	public static double[] scalar_multiply_inplace( final double[] a, final double x )
	{
		for( int i = 0; i < a.length; ++i ) {
			a[i] *= x;
		}
		return a;
	}
	
	public static double[] scalar_multiply( final double[] a, final double x )
	{
		final double[] result = Arrays.copyOf( a, a.length );
		scalar_multiply_inplace( result, x );
		return result;
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
//		final List<Integer> xs = new ArrayList<Integer>();
//		for( int i = 0; i < 10; ++i ) { xs.add( i ); }
//		final int[] drop = new int[] { 1, 3, 5, 7, 9 };
//		System.out.println( xs );
//		for( final Integer i : Fn.takeAll( Fn.remove( new Fn.IteratorSlice<Integer>( xs ), drop ) ) ) {
//			System.out.print( " " + i );
//		}
//		for( final Integer i : Fn.takeAll( Fn.keep( new Fn.IteratorSlice<Integer>( xs ), drop ) ) ) {
//			System.out.print( " " + i );
//		}
		
		final RandomGenerator rng = new MersenneTwister( 42 );
		final List<Integer> test = Arrays.asList( 0, 1, 2, 3, 4 );
		final int n = 100000;
		final int[] counts = new int[test.size()];
		for( int i = 0; i < n; ++i ) {
			final int choice = Fn.uniform_choice( rng, new Fn.ListSlice<Integer>( test, 0, test.size() ) );
			counts[choice] += 1;
		}
		System.out.println( Arrays.toString( counts ) );
	}

}
