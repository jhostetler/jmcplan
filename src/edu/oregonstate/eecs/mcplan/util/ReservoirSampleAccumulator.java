/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Constructs a uniform sample of size 'k' incrementally from an unbounded
 * stream of samples.
 */
public class ReservoirSampleAccumulator<T>
{
	public final int k;
	
	private final RandomGenerator rng_;
	private final ArrayList<T> samples_;
	
	private int n_ = 0;
	
	public ReservoirSampleAccumulator( final RandomGenerator rng, final int k )
	{
		this.k = k;
		rng_ = rng;
		samples_ = new ArrayList<T>( k );
	}
	
	/**
	 * Offer a new sample. Must not be called between 'acceptNext()' and the
	 * corresponding 'addPending()'.
	 * @param t
	 */
	public void add( final T t )
	{
		if( acceptNext() ) {
			addPending( t );
		}
	}
	
	private int pending_ = -1;
	
	/**
	 * Returns true if the next sample is accepted. The client should call
	 * 'addPending()' if and only if acceptNext() returns true.
	 * 
	 * This method together with 'addPending()' allow you to see if an object
	 * would be accepted before constructing it, which is useful if
	 * construction is expensive.
	 * @return
	 */
	public boolean acceptNext()
	{
		assert( pending_ < 0 );
		if( n_ < k ) {
			pending_ = n_;
		}
		else {
			final int j = rng_.nextInt( n_ + 1 );
			if( j < k ) {
				pending_ = j;
			}
			else {
				pending_ = -1;
			}
		}
		if( n_ < Integer.MAX_VALUE - 1 ) {
			n_ += 1;
		}
		return pending_ >= 0;
	}
	
	/**
	 * Add an already-accepted sample. Must be called if and only if the
	 * last call to 'acceptNext()' returned 'true'.
	 * @param t
	 */
	public void addPending( final T t )
	{
		assert( pending_ >= 0 );
		if( n_ <= k ) {
			samples_.add( t );
		}
		else {
			samples_.set( pending_, t );
		}
		pending_ = -1;
	}
	
	/**
	 * The current list of samples. The caller must not modify the list.
	 * @return
	 */
	public ArrayList<T> samples()
	{
		return samples_;
	}
	
	/**
	 * The number of samples seen so far.
	 * @return
	 */
	public int n()
	{
		return n_;
	}
}
