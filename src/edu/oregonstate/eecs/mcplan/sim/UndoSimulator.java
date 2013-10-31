package edu.oregonstate.eecs.mcplan.sim;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;



public interface UndoSimulator<S, A extends VirtualConstructor<A>>
{
	public abstract S state();

	public abstract void takeAction( final JointAction<A> a );

	public abstract void untakeLastAction();

	public abstract long depth();
	
	public abstract long t();
	
	public abstract int nagents();

	public abstract int[] turn();

	public abstract double[] reward();
	
	public abstract boolean isTerminalState();
	
	public abstract long horizon();

	public abstract String detailString();
}
