/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import edu.oregonstate.eecs.mcplan.State;


/**
 * Utility functions for working with trajectories. A trajectory is a
 * chain-structured state-action graph.
 * 
 * @author jhostetler
 */
public final class Trajectory
{
	private Trajectory()
	{ }
	
	public static <S extends State, A> void closeStates( final StateNode<S, A> sn )
	{
		final StateActionGraphVisitor<S, A> v = new StateActionGraphVisitor<S, A>() {
			@Override
			public void visitStateNode( final StateNode<S, A> sn )
			{
				sn.s.close();
			}
		};
		new TrajectoryTraversal<>( sn, v ).run();
	}
	
	public static <S, A> StateNode<S, A> nextState( final StateNode<S, A> sn )
	{
		for( final ActionNode<S, A> an : sn.succ() ) {
			for( final StateNode<S, A> snprime : an.succ() ) {
				return snprime;
			}
		}
		return null;
	}
	
	public static <S, A> double sumReward( final StateNode<S, A> sn )
	{
		final SumRewardsVisitor<S, A> v = new SumRewardsVisitor<>();
		final TrajectoryTraversal<S, A> traversal = new TrajectoryTraversal<>( sn, v );
		traversal.run();
		return v.getSum();
	}
}
