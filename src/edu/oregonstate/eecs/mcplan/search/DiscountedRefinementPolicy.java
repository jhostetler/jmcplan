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

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Countdown;

/**
 * @author jhostetler
 *
 */
public class DiscountedRefinementPolicy<S, A extends VirtualConstructor<A>>
	implements AnytimePolicy<S, A>
{
	private final NegamaxVisitor<S, A> visitor_;
	private final int max_depth_;
	private final int max_horizon_;
	private final double discount_;
	private final UndoSimulator<S, A> sim_;
	private final ActionGenerator<S, AnytimePolicy<S, A>> action_gen_;
	private final Policy<S, A> default_policy_;
	
	private S s_ = null;
	private long t_ = 0L;
	private Policy<S, A> policy_used_ = null;
	
	public DiscountedRefinementPolicy( final int max_depth,
								 final int max_horizon,
								 final double discount,
								 final UndoSimulator<S, A> sim,
								 final ActionGenerator<S, AnytimePolicy<S, A>> action_gen,
								 final NegamaxVisitor<S, A> visitor,
								 final Policy<S, A> default_policy )
	{
		visitor_ = visitor;
		max_depth_ = max_depth;
		max_horizon_ = max_horizon;
		discount_ = discount;
		sim_ = sim;
		action_gen_ = action_gen;
		default_policy_ = default_policy;
		System.out.println( "[DiscountedRefinementPolicy] discount = " + discount_ );
	}

	@Override
	public void setState( final S s, final long t )
	{
		s_ = s;
		t_ = t;
	}

	@Override
	public A getAction()
	{
		return getAction( Long.MAX_VALUE );
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		assert( policy_used_ != null );
		policy_used_.actionResult( sprime, r );
	}

	@Override
	public String getName()
	{
		return "DiscountedRefinement";
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
		System.out.println( "[DiscountedRefinement] getAction()" );
		final BoundedVisitor<S, A> bv
			= new BoundedVisitor<S, A>( visitor_, new Countdown( control ) );
		final DiscountedRefinementSearch<S, A> search
			= new DiscountedRefinementSearch<S, A>( sim_, action_gen_, bv, max_depth_, max_horizon_, discount_ );
		search.run();
		
		if( search.principalVariation() != null && search.principalVariation().actions.get( 0 ) != null ) {
			System.out.println( "[DiscountedRefinement] PV: " + search.principalVariation() );
			policy_used_ = search.principalVariation().actions.get( 0 ).policy_;
		}
		else {
			System.out.println( "[DiscountedRefinement] ! Using leaf heuristic" );
			policy_used_ = default_policy_;
		}
		
		policy_used_.setState( s_, t_ );
		return policy_used_.getAction();
	}
}
