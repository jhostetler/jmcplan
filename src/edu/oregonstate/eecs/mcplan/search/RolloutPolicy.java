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
package edu.oregonstate.eecs.mcplan.search;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.Tokenizable;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class RolloutPolicy<S extends Tokenizable<T>, T, A extends VirtualConstructor<A>>
	implements AnytimePolicy<S, A>
{
	private static final Logger log = LoggerFactory.getLogger( RolloutPolicy.class );
	
	private final UndoSimulator<S, A> sim_;
	private final ActionGenerator<S, ? extends A> action_gen_;
	private final double c_;
	private final List<Policy<S, A>> rollout_policies_;
	private final MctsNegamaxVisitor<S, A> visitor_;
	
	public RolloutPolicy( final UndoSimulator<S, A> sim, final ActionGenerator<S, ? extends A> action_gen,
						  final double c, final List<Policy<S, A>> rollout_policies, final MctsNegamaxVisitor<S, A> visitor )
	{
		sim_ = sim;
		action_gen_ = action_gen;
		c_ = c;
		rollout_policies_ = rollout_policies;
		visitor_ = visitor;
	}
	
	@Override
	public void setState( final S s, final long t )
	{ }

	@Override
	public A getAction()
	{
		return getAction( maxControl() );
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		// TODO:
	}

	@Override
	public String getName()
	{
		return "RolloutPolicy";
	}

	@Override
	public long minControl()
	{
		return 0;
	}

	@Override
	public long maxControl()
	{
		return Long.MAX_VALUE;
	}

	@Override
	public A getAction( final long control )
	{
		System.out.println( "[RolloutPolicy] getAction()" );
		final RolloutSearch<S, T, A> search = new RolloutSearch<S, T, A>(
			sim_, action_gen_, c_, control, rollout_policies_, visitor_ );
		search.run();
		final double[] qmean = new double[search.q_.length];
		final double[] qvar = new double[search.q_.length];
		for( int i = 0; i < search.q_.length; ++i ) {
			final MeanVarianceAccumulator mv = search.q_[i];
			qmean[i] = mv.mean();
			qvar[i] = mv.variance();
		}
		System.out.println( "Ping!" );
		log.info( "qmean = {}", Arrays.toString( qmean ) );
		log.info( "qvar = {}", Arrays.toString( qvar ) );
		log.info( "na = {}", Arrays.toString( search.na_ ) );
		log.info( "ns = {}", search.ns_ );
		return search.principalVariation().actions.get( 0 );
	}

}
