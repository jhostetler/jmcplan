/**
 * 
 */
package edu.oregonstate.eecs.mcplan.bandit;

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.collect.Iterables;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.sim.ActionNode;
import edu.oregonstate.eecs.mcplan.sim.SimulationListener;
import edu.oregonstate.eecs.mcplan.sim.StateNode;
import edu.oregonstate.eecs.mcplan.sim.TrajectorySimulator;

/**
 * @author jhostetler
 *
 */
public class PolicyRolloutEvaluator<S extends State, A> implements StochasticEvaluator<Policy<S, A>>
{
	private final class TrajectoryConsumer implements SimulationListener<S, A>
	{
		private double r = 0;
		private final ArrayList<S> states = new ArrayList<S>();
		
		public double consume()
		{
			final double rr = r;
			r = 0;
			for( final S s : states ) {
				s.close();
			}
			states.clear();
			return rr;
		}
		
		@Override
		public void onInitialStateSample( final StateNode<S, A> s0 )
		{
			r += s0.r;
			// Note: Don't save initial state because we don't want to close it
		}

		@Override
		public void onTransitionSample( final ActionNode<S, A> trans )
		{
			r += trans.r;
			final StateNode<S, A> succ = Iterables.getOnlyElement( trans.successors() );
			r += succ.r;
			states.add( succ.s );
//			for( final StateNode<S, A> succ : trans.successors() ) {
//				r += succ.r;
//				states.add( succ.s );
//				break;
//			}
		}
	}
	
	private final TrajectorySimulator<S, A> sim;
	private final S s0;
	
	public final int depth_limit;
	
	private final TrajectoryConsumer sum = new TrajectoryConsumer();
	
	/**
	 * @param sim
	 * @param s0 *Not* owned by PolicyRolloutEvaluator
	 * @param depth_limit
	 */
	public PolicyRolloutEvaluator( final TrajectorySimulator<S, A> sim, final S s0, final int depth_limit )
	{
		this.sim = sim;
		this.s0 = s0;
		this.depth_limit = depth_limit;
		sim.addSimulationListener( sum );
	}
	
	@Override
	public double evaluate( final RandomGenerator rng, final Policy<S, A> pi )
	{
		pi.reset();
		sim.sampleTrajectory( rng, s0, pi, depth_limit );
		final double r = sum.consume();
		return r;
	}
}
