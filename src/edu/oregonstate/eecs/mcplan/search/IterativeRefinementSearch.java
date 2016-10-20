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

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.DurativeAction;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VariableDurationActionGenerator;
import edu.oregonstate.eecs.mcplan.sim.DurativeActionSimulator;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;

/**
 * @author jhostetler
 *
 */
public class IterativeRefinementSearch<S, A extends UndoableAction<S, A>>
	implements GameTreeSearch<S, DurativeAction<S, A>>
{
	private final SimultaneousMoveSimulator<S, A> sim_;
	private final ActionGenerator<S, AnytimePolicy<S, A>> action_gen_;
	private final NegamaxVisitor<S, A> visitor_;
	private final int max_depth_;
	private final int max_horizon_;
	
	private PrincipalVariation<S, DurativeAction<S, A>> pv_ = null;
	private boolean complete_ = false;
	
	public IterativeRefinementSearch( final SimultaneousMoveSimulator<S, A> sim,
									  final ActionGenerator<S, AnytimePolicy<S, A>> action_gen,
									  final NegamaxVisitor<S, A> visitor,
									  final int max_depth, final int max_horizon )
	{
		sim_ = sim;
		action_gen_ = action_gen;
		visitor_ = visitor;
		max_depth_ = max_depth;
		max_horizon_ = max_horizon;
	}
	
	@Override
	public double score()
	{
		return pv_.score;
	}
	
	@Override
	public PrincipalVariation<S, DurativeAction<S, A>> principalVariation()
	{
		return pv_;
	}
	
	@Override
	public boolean isComplete()
	{
		return complete_;
	}
	
	/**
	 * Splits 'n' into 'depth' intervals of (almost) equal size. Any remainder
	 * is spread evenly between intervals starting with the intervals
	 * furthest in the future. This has an implicit discounting effect, as
	 * search is more precise closer to the current time.
	 * @param n
	 * @param depth
	 * @return
	 */
	private int[] optimalSplit( final int n, final int depth )
	{
		final int[] idx = new int[depth];
		final int d = n / depth;
		Arrays.fill( idx, d );
		// Distribute the remainder starting with the periods
		// furthest in the future
		for( int i = 0; i < n % depth; ++i ) {
			idx[(depth - 1) - i] += 1;
		}
		return idx;
	}
	
	@Override
	public void run()
	{
		System.out.println( "[IterativeRefinementSearch] run(): max_depth_ = " + max_depth_ );
		int depth = 1;
		while( depth <= max_depth_ ) {
			final int H = (int) Math.min( sim_.horizon(), max_horizon_ );
			if( depth > H ) {
				break;
			}
			final int[] idx = optimalSplit( H, depth );
			System.out.println( "[IterativeRefinementSearch] idx = " + Arrays.toString( idx ) );
			final DurativeActionSimulator<S, A> durative_sim = new DurativeActionSimulator<S, A>( sim_ );
			final DurativeNegamaxVisitor<S, A> durative_visitor;
			if( pv_ == null ) {
				durative_visitor = new DurativeNegamaxVisitor<S, A>( visitor_ );
			}
			else {
				// Principal variation move ordering heuristic
				durative_visitor = new DurativePvMoveOrdering<S, A>( visitor_, pv_, idx );
			}
			final VariableDurationActionGenerator<S, A> durative_gen
				= new VariableDurationActionGenerator<S, A>( action_gen_, idx, durative_visitor );
			
//			final IterativeDeepeningSearch<S, DurativeUndoableAction<S, A>> search
//				= new IterativeDeepeningSearch<S, DurativeUndoableAction<S, A>>(
//					durative_sim, durative_gen.create(),
//					new DurativeNegamaxVisitor<S, A>( visitor_, policy_epoch ), depth );
			final NegamaxSearch<S, DurativeAction<S, A>> search
				= new NegamaxSearch<S, DurativeAction<S, A>>(
					durative_sim, depth * sim_.nagents(), durative_gen.create(),
					durative_visitor );
			final long start = System.currentTimeMillis();
			search.run();
			final long stop = System.currentTimeMillis();
			
			// TODO: Equivalent of PvMoveOrdering.
			if( search.principalVariation() != null
				&& (pv_ == null || search.isComplete()
					|| (search.principalVariation() != null
						&& search.principalVariation().isNarrowerThan( pv_ ))) ) {
				pv_ = search.principalVariation();
				System.out.println( "[IterativeRefinementSearch] Updating PV" );
			}
			System.out.println( "[IterativeRefinementSearch] Depth: " + depth );
			System.out.println( "[IterativeRefinementSearch] PV: " + pv_ );
			System.out.println( "[IterativeRefinementSearch] Time: " + (stop - start) + " ms" );
			
			if( !search.isComplete() ) {
				break;
			}
			
			++depth;
		}
		complete_ = true;
	}
}
