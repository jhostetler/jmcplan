/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.Simulator;

/**
 * @author jhostetler
 *
 */
public interface EvaluationFunction<S extends State, A extends VirtualConstructor<A>>
{
	/**
	 * FIXME: Making the argument a Simulator is a pragmatic choice, but
	 * it precludes things like a rollout evaluator that does more than
	 * one rollout.
	 * 
	 * @param sim
	 * @return
	 */
	public double[] evaluate( final Simulator<S, A> sim );
}
