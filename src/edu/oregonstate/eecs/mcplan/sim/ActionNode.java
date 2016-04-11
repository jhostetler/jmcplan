/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import java.util.ArrayList;

/**
 * @author jhostetler
 *
 */
public class ActionNode<S, A>
{
	public final A a;
	public final double r;
	private final ArrayList<StateNode<S, A>> succ = new ArrayList<>();
	
	public ActionNode( final A a, final double r )
	{
		this.a = a;
		this.r = r;
	}
	
	public Iterable<StateNode<S, A>> succ()
	{
		return succ;
	}
	
	public void addSuccessor( final StateNode<S, A> sn )
	{
		succ.add( sn );
	}
}
