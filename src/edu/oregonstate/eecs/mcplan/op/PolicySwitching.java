/**
 * 
 */
package edu.oregonstate.eecs.mcplan.op;

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.bandit.FiniteBandit;
import edu.oregonstate.eecs.mcplan.bandit.PolicyRolloutEvaluator;
import edu.oregonstate.eecs.mcplan.sim.TrajectorySimulator;

/**
 * @author jhostetler
 *
 */
public class PolicySwitching<S extends State, A extends VirtualConstructor<A>> extends AnytimePolicy<S, A>
{
	private final RandomGenerator rng;
	protected final TrajectorySimulator<S, A> sim;
	private final FiniteBandit<Policy<S, A>> bandit_prototype;
	protected final ArrayList<Policy<S, A>> Pi;
	private final int depth_limit;
	
	private S s = null;
	private long t = 0;
	private FiniteBandit<Policy<S, A>> bandit = null;
	private Policy<S, A> pistar = null;
	
	public PolicySwitching( final RandomGenerator rng,
						  	final TrajectorySimulator<S, A> sim,
						  	final FiniteBandit<Policy<S, A>> bandit,
						  	final ArrayList<Policy<S, A>> Pi,
						  	final int depth_limit )
	{
		this.rng = rng;
		this.sim = sim;
		this.bandit_prototype = bandit;
		this.Pi = Pi;
		this.depth_limit = depth_limit;
	}
	
	protected ArrayList<Policy<S, A>> getPolicies( final S s )
	{
		return Pi;
	}

	@Override
	public final boolean improvePolicy()
	{
		// FIXME: Configurable convergence threshold? This is a bit of a hack
		// to allow us to stop early in deterministic settings when using a
		// budget.
		if( bandit.convergenceTest( 0, 1 ) ) {
			return false;
		}
		else {
			bandit.sampleArm( rng );
			pistar = bandit.bestArm();
			return true;
		}
	}

	@Override
	public final void setState( final S s, final long t )
	{
		this.s = s;
		this.t = t;
		
		// Initialize bandit sampler
		bandit = bandit_prototype.create(
			getPolicies( s ), new PolicyRolloutEvaluator<S, A>( sim, s, depth_limit ) );
		
		// Set default action
		final int ai = rng.nextInt( bandit.Narms() );
		pistar = bandit.arm( ai );
	}

	@Override
	public final A getAction()
	{
		pistar.reset();
		pistar.setState( s, t );
		return pistar.getAction();
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{
		return "PolicySwitching";
	}

	@Override
	public int hashCode()
	{
		return System.identityHashCode( this );
	}

	@Override
	public boolean equals( final Object that )
	{
		return this == that;
	}
}
