/**
 * 
 */
package edu.oregonstate.eecs.mcplan.experiments;

import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;

/**
 * TODO: Instance only has to be Copyable because of MultipleInstanceMultipleWorldGenerator,
 * which, in turn, only needs to be able to recover the initial state. So,
 * it's not really 'Copyable' so much as "resetable-to-initial-state".
 */
public abstract class Instance<I, S, A extends UndoableAction<S> & VirtualConstructor<A>>
	implements CsvWriter, Copyable<I>
{
	public abstract UndoSimulator<S, A> simulator();
	public abstract S state();
	public abstract int nextSeed();
}
