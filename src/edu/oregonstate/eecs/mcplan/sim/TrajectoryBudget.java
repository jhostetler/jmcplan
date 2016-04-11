/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import edu.oregonstate.eecs.mcplan.search.fsss.Budget;

/**
 * @author jhostetler
 *
 */
public class TrajectoryBudget<S, A> implements Budget, SimulationListener<S, A>
{
	private int trajectories = 0;
	public final int budget;
	
	public TrajectoryBudget( final int budget )
	{
		this.budget = budget;
	}
	
	@Override
	public void onInitialStateSample( final StateNode<S, A> s0 )
	{ }
	
	@Override
	public void onTransitionSample( final ActionNode<S, A> trans )
	{ }

	@Override
	public void onTrajectorySample( final StateNode<S, A> traj )
	{
		trajectories += 1;
	}

	@Override
	public boolean isExceeded()
	{
		return trajectories >= budget;
	}

	@Override
	public double actualDouble()
	{
		return trajectories;
	}

	@Override
	public void reset()
	{
		trajectories = 0;
	}
}
