/**
 * 
 */
package edu.oregonstate.eecs.mcplan.experiments;

import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;

/**
 * TODO: Instance only has to be Copyable because of MultipleInstanceMultipleWorldGenerator,
 * which, in turn, only needs to be able to recover the initial state. So,
 * it's not really 'Copyable' so much as "resetable-to-initial-state".
 */
public abstract class Instance<I, S, A extends UndoableAction<S>> implements CsvWriter, Copyable<I>
{
	public abstract SimultaneousMoveSimulator<S, A> simulator();
	public abstract S state();
	public abstract long nextSeed();
}
