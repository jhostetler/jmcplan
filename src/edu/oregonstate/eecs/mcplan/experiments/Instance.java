/**
 * 
 */
package edu.oregonstate.eecs.mcplan.experiments;

import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;

/**
 * @author jhostetler
 *
 */
public abstract class Instance<I, S, A extends UndoableAction<S, A>> implements Copyable<I>, CsvWriter
{
	public abstract SimultaneousMoveSimulator<S, A> simulator();
	public abstract S state();
}
