/**
 * 
 */
package edu.oregonstate.eecs.mcplan.rl;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class UcbBandit<T>
{
	public final ArrayList<T> arms;
	public final MeanVarianceAccumulator[] values;
	public final int[] counts;
	public int N = 0;
	
	public final double c;
	
	private static final int nothing_pending = -1;
	private int pending = nothing_pending;
	
	public UcbBandit( final ArrayList<T> arms, final double c )
	{
		assert( arms.size() > 0 );
		this.arms = arms;
		values = new MeanVarianceAccumulator[arms.size()];
		for( int i = 0; i < arms.size(); ++i ) {
			values[i] = new MeanVarianceAccumulator();
		}
		counts = new int[arms.size()];
		
		this.c = c;
	}
	
	/**
	 * Sample an arm index. Each call to sample() must be followed by a call to
	 * update().
	 * @see update(double)
	 * @return
	 */
	public int sample()
	{
		if( pending != nothing_pending ) {
			throw new IllegalStateException( "update() has not been called since last sample()" );
		}
		
		double max_value = -Double.MAX_VALUE;
		for( int i = 0; i < arms.size(); ++i ) {
			if( counts[i] == 0 ) {
				pending = i;
				break;
			}
			else {
				final double exploit = values[i].mean();
				final double explore = c * Math.sqrt( Math.log( N ) / counts[i] );
				final double v = explore + exploit;
				if( v > max_value ) {
					pending = i;
					max_value = v;
				}
			}
		}
		
		assert( pending >= 0 );
		return pending;
	}
	
	/**
	 * Update the value of the last arm sampled.
	 * @see sample()
	 * @param v
	 */
	public void update( final double v )
	{
		if( pending == nothing_pending ) {
			throw new IllegalStateException( "sample() has not been called since last update()" );
		}
		
		values[pending].add( v );
		counts[pending] += 1;
		N += 1;
		
		pending = nothing_pending;
	}
}
