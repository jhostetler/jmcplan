/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.Policy;

/**
 * @author jhostetler
 *
 */
public abstract class TrajectorySimulator<S, A>
{
	private final List<SimulationListener<S, A>> listeners = new ArrayList<>();
	
	public final void sampleTrajectory( final RandomGenerator rng, final S s, final Policy<S, A> pi )
	{
		sampleTrajectory( rng, s, pi, 0 );
	}
	
	public abstract void sampleTrajectory(
		final RandomGenerator rng, final S s, final Policy<S, A> pi, final int T );
	
	public final void addSimulationListener( final SimulationListener<S, A> listener )
	{
		listeners.add( listener );
	}
	
	protected final void fireInitialStateSample( final StateNode<S, A> s0 )
	{
		for( final SimulationListener<S, A> listener : listeners ) {
			listener.onInitialStateSample( s0 );
		}
	}
	
	protected final void fireTransitionSample( final ActionNode<S, A> trans )
	{
		for( final SimulationListener<S, A> listener : listeners ) {
			listener.onTransitionSample( trans );
		}
	}
}
