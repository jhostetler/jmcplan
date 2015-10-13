/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public interface Action<S>
{
	public abstract void doAction( final RandomGenerator rng, final S s );
}
