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
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;

/**
 * @author jhostetler
 *
 */
public class IterativeDeepeningSearch<S, A extends UndoableAction<S, A>> implements GameTreeSearch<S, A>
{
	private final UndoSimulator<S, A> sim_;
	private final ActionGenerator<S, A> action_gen_;
	private final NegamaxVisitor<S, A> visitor_;
	private final int max_depth_;
	
	private PrincipalVariation<S, A> pv_ = null;
	private boolean complete_ = false;
	
	public IterativeDeepeningSearch( final UndoSimulator<S, A> sim, final ActionGenerator<S, A> action_gen,
									 final NegamaxVisitor<S, A> visitor, final int max_depth )
	{
		sim_ = sim;
		action_gen_ = action_gen;
		visitor_ = visitor;
		max_depth_ = max_depth;
	}
	
	@Override
	public double score()
	{
		return pv_.score;
	}
	
	@Override
	public PrincipalVariation<S, A> principalVariation()
	{
		return pv_;
	}
	
	@Override
	public boolean isComplete()
	{
		return complete_;
	}
	
	@Override
	public void run()
	{
		System.out.println( "[IterativeDeepeningSearch] run(): max_depth_ = " + max_depth_ );
		ActionGenerator<S, A> local_gen = action_gen_.create();
		int depth = 1;
		while( depth <= max_depth_ ) {
			if( depth > sim_.horizon() ) {
				break;
			}
			final NegamaxSearch<S, A> search = new NegamaxSearch<S, A>(
				sim_, depth * sim_.nagents(), local_gen, visitor_ );
			final long start = System.currentTimeMillis();
			search.run();
			final long stop = System.currentTimeMillis();
			
			System.out.println( "[IterativeDeepeningSearch] search.isComplete() = " + search.isComplete() );
			if( search.principalVariation() != null
				&& (pv_ == null || search.isComplete()
					|| (search.principalVariation() != null
						&& search.principalVariation().isNarrowerThan( pv_ ))) ) {
				if( pv_ == null ) {
					System.out.println( "[IterativeDeepeningSearch] pv_ was NULL" );
				}
				else {
					System.out.println( "[IterativeDeepeningSearch] Updating PV ("
										+ pv_.alpha + ", " + pv_.beta + ") -> ("
										+ search.principalVariation().alpha + ", "
										+ search.principalVariation().beta + ")" );
				}
				pv_ = search.principalVariation();
				local_gen = new PvMoveOrdering<S, A>( action_gen_, pv_ );
			}
			else {
				if( search.principalVariation() != null ) {
					System.out.println( "[IterativeDeepeningSearch] Not updating PV ("
										+ pv_.alpha + ", " + pv_.beta + ") -> ("
										+ search.principalVariation().alpha + ", "
										+ search.principalVariation().beta + ")" );
				}
				else if( pv_ != null ) {
					System.out.println( "[IterativeDeepeningSearch] Not updating PV ("
										+ pv_.alpha + ", " + pv_.beta + ") -> NULL" );
				}
				else {
					System.out.println( "[IterativeDeepeningSearch] Not updating PV NULL -> NULL" );
				}
				local_gen = action_gen_.create();
			}
			System.out.println( "[IterativeDeepeningSearch] Depth: " + depth );
			System.out.println( "[IterativeDeepeningSearch] PV: " + pv_ );
			System.out.println( "[IterativeDeepeningSearch] Time: " + (stop - start) + " ms" );
			
			if( !search.isComplete() ) {
				break;
			}
			
			++depth;
		}
		complete_ = true;
	}
}
