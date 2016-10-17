/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import java.util.ArrayList;

import com.google.common.collect.Iterables;

/**
 * @author jhostetler
 *
 */
public class ActionNode<S, A>
{
	public final A a;
	public final double r;
	protected final ArrayList<StateNode<S, A>> successors = new ArrayList<>();
	
	public ActionNode( final A a, final double r )
	{
		this.a = a;
		this.r = r;
	}
	
	public Iterable<StateNode<S, A>> successors()
	{
		return Iterables.unmodifiableIterable( successors );
	}
	
	public void addSuccessor( final StateNode<S, A> sn )
	{
		successors.add( sn );
	}
}
