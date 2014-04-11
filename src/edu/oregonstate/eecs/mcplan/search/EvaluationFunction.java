/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;

/**
 * @author jhostetler
 *
 */
public interface EvaluationFunction<S extends State, A extends VirtualConstructor<A>>
{
	public double[] evaluate( final UndoSimulator<S, A> sim );
}
