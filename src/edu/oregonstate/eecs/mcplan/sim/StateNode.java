package edu.oregonstate.eecs.mcplan.sim;

import java.util.ArrayList;

import com.google.common.collect.Iterables;



public class StateNode<S, A>
{
	public final S s;
	public final double r;
	protected final ArrayList<ActionNode<S, A>> successors = new ArrayList<>();
	
	public StateNode( final S s, final double r )
	{
		this.s = s;
		this.r = r;
	}
	
	public Iterable<ActionNode<S, A>> successors()
	{
		return Iterables.unmodifiableIterable( successors );
	}
	
	public void addSuccessor( final ActionNode<S, A> an )
	{
		successors.add( an );
	}
	
	public boolean isTerminal()
	{
		return successors.isEmpty();
	}
}
