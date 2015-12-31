/**
 * 
 */
package edu.oregonstate.eecs.mcplan.bandit;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.sim.Trajectory;
import edu.oregonstate.eecs.mcplan.sim.TrajectorySimulator;

/**
 * @author jhostetler
 *
 */
public class PolicyRolloutEvaluator<S, A> implements StochasticEvaluator<Policy<S, A>>
{
	private final TrajectorySimulator<S, A> sim;
	private final S s0;
	
	public final int depth_limit;
	
	public PolicyRolloutEvaluator( final TrajectorySimulator<S, A> sim, final S s0, final int depth_limit )
	{
		this.sim = sim;
		this.s0 = s0;
		this.depth_limit = depth_limit;
	}
	
	@Override
	public double evaluate( final RandomGenerator rng, final Policy<S, A> pi )
	{
		final Trajectory<S, A> t = sim.sampleTrajectory( rng, s0, pi, depth_limit );
		return t.sumReward();
	}
}
