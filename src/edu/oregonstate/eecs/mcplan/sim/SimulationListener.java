/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

/**
 * @author jhostetler
 *
 */
public interface SimulationListener<S, A>
{
	public void onInitialStateSample( final StateNode<S, A> s0 );
	public void onTransitionSample( final ActionNode<S, A> trans );
}
