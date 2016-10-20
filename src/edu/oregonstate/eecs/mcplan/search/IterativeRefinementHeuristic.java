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
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;

/**
 * @author jhostetler
 *
 */
public class IterativeRefinementHeuristic<S, A extends UndoableAction<S, A>>
	extends ForwardingNegamaxVisitor<S, A>
{
	private final int max_depth_;
	private final int max_horizon_;
	private final SimultaneousMoveSimulator<S, A> sim_;
	private final ActionGenerator<S, A> action_gen_;
	
	public IterativeRefinementHeuristic( final NegamaxVisitor<S, A> inner,
										 final int max_depth,
										 final int max_horizon,
										 final SimultaneousMoveSimulator<S, A> sim,
										 final ActionGenerator<S, A> action_gen )
	{
		super( inner );
		max_depth_ = max_depth;
		max_horizon_ = max_horizon;
		sim_ = sim;
		action_gen_ = action_gen;
	}
	
	@Override
	public double heuristic( final S s )
	{
		System.out.println( "[IterativeRefinementHeuristic] heuristic()" );
		final IterativeRefinementSearch<S, A> search
			= new IterativeRefinementSearch<S, A>( sim_, action_gen_, inner_, max_depth_, max_horizon_ );
		search.run();
		
		if( search.principalVariation() != null ) {
			System.out.println( "[IterativeRefinementHeuristic] PV: " + search.principalVariation() );
			// Heuristic value is score of principal variation
			return search.principalVariation().score;
		}
		else {
			System.out.println( "[IterativeRefinementHeuristic] ! Using leaf heuristic" );
			return inner_.heuristic( s );
		}
	}
	
}
