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
		super( rng, sim, bandit, Pi, depth_limit );
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
