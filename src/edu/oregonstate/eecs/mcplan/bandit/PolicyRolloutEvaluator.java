/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
public class PolicyRolloutEvaluator<S extends State, A> extends StochasticEvaluator<Policy<S, A>>
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
	private final double Rmax;
	
	private final TrajectoryConsumer sum = new TrajectoryConsumer();
	
	/**
	 * @param sim
	 * @param s0 *Not* owned by PolicyRolloutEvaluator
	 * @param depth_limit
	 */
	public PolicyRolloutEvaluator( final TrajectorySimulator<S, A> sim, final S s0, final int depth_limit )
	{
		this( sim, s0, depth_limit, Double.POSITIVE_INFINITY );
	}
	
	/**
	 * @param sim
	 * @param s0 *Not* owned by PolicyRolloutEvaluator
	 * @param depth_limit
	 */
	public PolicyRolloutEvaluator( final TrajectorySimulator<S, A> sim, final S s0,
								   final int depth_limit, final double Rmax )
	{
		this.sim = sim;
		this.s0 = s0;
		this.depth_limit = depth_limit;
		this.Rmax = Rmax;
		sim.addSimulationListener( sum );
	}
	
	@Override
	public double evaluate( final RandomGenerator rng, final Policy<S, A> pi )
	{
		final Policy<S, A> pi_copy = pi.copy();
		sim.sampleTrajectory( rng, s0, pi_copy, depth_limit );
		final double r = sum.consume();
		return r;
	}

	@Override
	public double Vmax()
	{
		return Rmax * depth_limit;
	}
}
