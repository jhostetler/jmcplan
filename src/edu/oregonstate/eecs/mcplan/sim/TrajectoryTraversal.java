/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * Traverses the "left-most" trajectory in the graph, that is the trajectory
 * formed by following the first successor of every node. Will run forever if
 * the leftmost trajectory is a cycle.
 */
public class TrajectoryTraversal<S, A> extends StateActionGraphTraversal<S, A>
{
	public TrajectoryTraversal( final StateNode<S, A> s0 )
	{
		super( s0 );
	}
	
	public TrajectoryTraversal( final StateNode<S, A> s0, final StateActionGraphVisitor<S, A> visitor )
	{
		super( s0, visitor );
	}

	@Override
	public void run()
	{
		StateNode<S, A> s = s0;
		fireStateNode( s );
		while( !s.isTerminal() ) {
			final ActionNode<S, A> a = Fn.head( s.succ() );
			fireActionNode( a );
			s = Fn.head( a.succ() );
			fireStateNode( s );
		}
	}
}
