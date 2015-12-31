/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.Policy;

/**
 * @author jhostetler
 *
 */
public abstract class TrajectorySimulator<S, A>
{
	public final Trajectory<S, A> sampleTrajectory( final RandomGenerator rng, final S s, final Policy<S, A> pi )
	{
		return sampleTrajectory( rng, s, pi, 0 );
	}
	
	public abstract Trajectory<S, A> sampleTrajectory(
		final RandomGenerator rng, final S s, final Policy<S, A> pi, final int T );
}
