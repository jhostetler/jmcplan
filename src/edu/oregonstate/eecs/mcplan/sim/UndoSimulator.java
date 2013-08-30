package edu.oregonstate.eecs.mcplan.sim;



public interface UndoSimulator<S, A>
{
	public abstract S state();

	public abstract void takeAction( final A a );

	public abstract void untakeLastAction();

	public abstract long depth();
	
	public abstract long t();
	
	public abstract int getNumAgents();

	public abstract int getTurn();

	public abstract double[] getReward();
	
	public abstract boolean isTerminalState();
	
	public abstract long horizon();

	public abstract String detailString();
}
