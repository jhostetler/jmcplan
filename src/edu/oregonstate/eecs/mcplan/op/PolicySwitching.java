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

import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.LoggerManager;
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
	private final ch.qos.logback.classic.Logger LogAgent = LoggerManager.getLogger( "log.agent" );
	
	private final RandomGenerator rng;
	protected final TrajectorySimulator<S, A> sim;
	private final FiniteBandit<Policy<S, A>> bandit_prototype;
	protected final ArrayList<Policy<S, A>> Pi;
	private final int depth_limit;
	private final double Rmax;
	
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
		this( rng, sim, bandit, Pi, depth_limit, Double.POSITIVE_INFINITY );
	}
	
	public PolicySwitching( final RandomGenerator rng,
						  	final TrajectorySimulator<S, A> sim,
						  	final FiniteBandit<Policy<S, A>> bandit,
						  	final ArrayList<Policy<S, A>> Pi,
						  	final int depth_limit,
						  	final double Rmax )
	{
		this.rng = rng;
		this.sim = sim;
		this.bandit_prototype = bandit;
		this.Pi = Pi;
		this.depth_limit = depth_limit;
		this.Rmax = Rmax;
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
		
//		LogAgent.debug( "PolicySwitching: setState()" );
		
		final ArrayList<Policy<S, A>> Pi_s = getPolicies( s );
		for( final Policy<S, A> pi : Pi_s ) {
			pi.setState( s, t );
//			LogAgent.debug( "\t{}", pi );
		}
		
		// Initialize bandit sampler
		bandit = bandit_prototype.create(
			Pi_s, new PolicyRolloutEvaluator<S, A>( sim, s, depth_limit, Rmax ) );
		
		// Set default action
		final int ai = rng.nextInt( bandit.Narms() );
		pistar = bandit.arm( ai );
	}

	@Override
	public final A getAction()
	{
//		pistar.reset();
//		pistar.setState( s, t );
		
		final Policy<S, A> pistar_copy = pistar.copy();
//		LogAgent.debug( "PolicySwitching: selected {}", pistar_copy );
		
		return pistar_copy.getAction();
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
