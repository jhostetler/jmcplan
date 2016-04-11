/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import java.util.ArrayList;
import java.util.List;

/**
 * Traverses a state-action graph defined by StateNode and ActionNode objects,
 * and fires events when encountering nodes.
 * 
 * @author jhostetler
 */
public abstract class StateActionGraphTraversal<S, A> implements Runnable
{
	protected final StateNode<S, A> s0;
	private final List<StateActionGraphVisitor<S, A>> visitors = new ArrayList<>();
	
	public StateActionGraphTraversal( final StateNode<S, A> s0 )
	{
		this.s0 = s0;
	}
	
	public StateActionGraphTraversal( final StateNode<S, A> s0, final StateActionGraphVisitor<S, A> visitor )
	{
		this( s0 );
		visitors.add( visitor );
	}
	
	public void addVisitor( final StateActionGraphVisitor<S, A> visitor )
	{
		visitors.add( visitor );
	}
	
	protected void fireStateNode( final StateNode<S, A> sn )
	{
		for( final StateActionGraphVisitor<S, A> visitor : visitors ) {
			visitor.visitStateNode( sn );
		}
	}
	
	protected void fireActionNode( final ActionNode<S, A> an )
	{
		for( final StateActionGraphVisitor<S, A> visitor : visitors ) {
			visitor.visitActionNode( an );
		}
	}
}
