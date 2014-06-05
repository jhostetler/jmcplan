/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.frogger;

import edu.oregonstate.eecs.mcplan.search.EvaluationFunction;
import edu.oregonstate.eecs.mcplan.sim.Simulator;

/**
 * Estimates the cost-to-go as the number of lanes left to cross (ie. the
 * minimum possible number of steps).
 */
public class LanesToGoHeuristic implements EvaluationFunction<FroggerState, FroggerAction>
{
	@Override
	public double[] evaluate( final Simulator<FroggerState, FroggerAction> sim )
	{
		final FroggerState s = sim.state();
		return new double[] { -(s.params.lanes + 1 - s.frog_y) };
	}
}
