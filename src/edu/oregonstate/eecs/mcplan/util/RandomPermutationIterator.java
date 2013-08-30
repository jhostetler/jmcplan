/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import org.apache.commons.math3.random.RandomGenerator;


/**
 * @author jhostetler
 *
 */
public class RandomPermutationIterator<T> extends Generator<T>
{
	private final T[] x_;
	private final int[] permute_;
	
	private int idx_ = 0;
	
	public RandomPermutationIterator( final T[] x, final int[] permute )
	{
		x_ = x;
		permute_ = permute;
	}
	
	public RandomPermutationIterator( final T[] x, final RandomGenerator rng )
	{
		x_ = x;
		permute_ = new int[x_.length];
		for( int i = 0; i < permute_.length; ++i ) {
			permute_[i] = i;
		}
		ListUtil.randomShuffle( rng, permute_ );
	}
	
	public int[] permutation()
	{
		return permute_;
	}
	
	@Override
	public boolean hasNext()
	{
		return idx_ < x_.length;
	}

	@Override
	public T next()
	{
		return x_[permute_[idx_++]];
	}
	
}
