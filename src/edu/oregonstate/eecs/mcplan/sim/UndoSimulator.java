package edu.oregonstate.eecs.mcplan.sim;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;



public interface UndoSimulator<S, A extends VirtualConstructor<A>> extends Simulator<S, A>
{
	public abstract void untakeLastAction();
}
