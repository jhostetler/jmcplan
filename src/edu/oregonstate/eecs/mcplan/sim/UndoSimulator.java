package edu.oregonstate.eecs.mcplan.sim;

import edu.oregonstate.eecs.mcplan.UndoableAction;


public interface UndoSimulator<S, A extends UndoableAction<S, A>>
{
	public abstract S state();

	public abstract void takeAction( final A a );

	public abstract void untakeLastAction();

	public abstract long depth();
	
	public abstract int getNumAgents();

	public abstract int getTurn();

	public abstract double getReward();
	
	public abstract boolean isTerminalState( final S s );
	
	public abstract long horizon();

	public abstract String detailString();
}
