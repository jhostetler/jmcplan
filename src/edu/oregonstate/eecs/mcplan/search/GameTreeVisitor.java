/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;


/**
 * A visitor for game trees. Uses the "double dispatch" pattern to determine
 * the type of each node polymorphically.
 */
public interface GameTreeVisitor<S, A extends VirtualConstructor<A>>
{
	public abstract void visit( final StateNode<S, A> s );
	public abstract void visit( final ActionNode<S, A> a );
}
