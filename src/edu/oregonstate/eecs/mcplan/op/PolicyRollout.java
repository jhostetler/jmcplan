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

/**
 * 
 */
package edu.oregonstate.eecs.mcplan.op;

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.ActionSet;
import edu.oregonstate.eecs.mcplan.ActionSpace;
import edu.oregonstate.eecs.mcplan.ConsPolicy;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.bandit.FiniteBandit;
import edu.oregonstate.eecs.mcplan.sim.TrajectorySimulator;

/**
 * In policy rollout, we select the action
 * <code>a* = argmax_{a \in \Actions} \max_{\pi \in \Pi} Q^{\pi}(s, a)</code>.
 * <p>
 * We implement policy rollout as policy switching over the policy set
 * <code>\Actions \times \Pi</code>
 * of policies that play a fixed action first, and then follow a fixed policy.
 */
public class PolicyRollout<S extends State, A extends VirtualConstructor<A>> extends PolicySwitching<S, A>
{
	private final ActionSpace<S, A> action_space;
	
	public PolicyRollout( final RandomGenerator rng,
						  final TrajectorySimulator<S, A> sim,
						  final ActionSpace<S, A> action_space,
						  final FiniteBandit<Policy<S, A>> bandit,
						  final ArrayList<Policy<S, A>> Pi,
						  final int depth_limit )
	{
		this( rng, sim, action_space, bandit, Pi, depth_limit, Double.POSITIVE_INFINITY );
	}
	
	public PolicyRollout( final RandomGenerator rng,
						  final TrajectorySimulator<S, A> sim,
						  final ActionSpace<S, A> action_space,
						  final FiniteBandit<Policy<S, A>> bandit,
						  final ArrayList<Policy<S, A>> Pi,
						  final int depth_limit,
						  final double Rmax )
	{
		super( rng, sim, bandit, Pi, depth_limit, Rmax );
		this.action_space = action_space;
	}
	
	@Override
	protected ArrayList<Policy<S, A>> getPolicies( final S s )
	{
		final ArrayList<Policy<S, A>> Pi_s = new ArrayList<Policy<S, A>>();
		final ActionSet<S, A> actions = action_space.getActionSet( s );
		
		for( final A a : actions ) {
			for( final Policy<S, A> pi : Pi ) {
				Pi_s.add( new ConsPolicy<>( a, pi ) );
			}
		}
		return Pi_s;
	}

	@Override
	public String getName()
	{
		return "PolicyRollout";
	}
}
