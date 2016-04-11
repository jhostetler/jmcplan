package edu.oregonstate.eecs.mcplan.sim;

import java.util.ArrayList;



public final class StateNode<S, A>
{
	public final S s;
	public final double r;
	private final ArrayList<ActionNode<S, A>> succ = new ArrayList<>();
	
	public StateNode( final S s, final double r )
	{
		this.s = s;
		this.r = r;
	}
	
	public Iterable<ActionNode<S, A>> succ()
	{
		return succ;
	}
	
	public void addSuccessor( final ActionNode<S, A> an )
	{
		succ.add( an );
	}
	
	public boolean isTerminal()
	{
		return succ.isEmpty();
	}
}
