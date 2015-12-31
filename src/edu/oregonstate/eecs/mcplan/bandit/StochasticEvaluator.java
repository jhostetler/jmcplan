/**
 * 
 */
package edu.oregonstate.eecs.mcplan.bandit;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public interface StochasticEvaluator<T>
{
	public abstract double evaluate( final RandomGenerator rng, final T t );
}
