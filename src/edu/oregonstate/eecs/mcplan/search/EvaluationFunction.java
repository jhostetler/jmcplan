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
	 * Calculates the estimate future return starting in 'sim.state()'.
	 * 
	 * The evaluator should include the immediate reward in the starting state
	 * (sim.reward()) ordinarily, unless you know better.
	 * 
	 * Contract for caller:
	 *     1. evaluate( sim ) must not be called when sim.state().isTerminal() == true
	 *     2. The caller must assume that evaluate() includes the immediate
	 *        reward in the start state.
	 * 
	 * FIXME: Making the argument a Simulator is a pragmatic choice, but
	 * it precludes things like a rollout evaluator that does more than
	 * one rollout.
	 * 
	 * @param sim
	 * @return
	 */
	public double[] evaluate( final Simulator<S, A> sim );
}
