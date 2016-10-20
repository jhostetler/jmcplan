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
package edu.oregonstate.eecs.mcplan.sim;

import java.util.ArrayDeque;
import java.util.Deque;

import edu.oregonstate.eecs.mcplan.DurativeAction;
import edu.oregonstate.eecs.mcplan.UndoableAction;



/**
 * @author jhostetler
 *
 */
public class DurativeActionSimulator<S, A extends UndoableAction<S>>
	implements UndoSimulator<S, DurativeAction<S, A>>
{
	private final SimultaneousMoveSimulator<S, A> base_sim_;
	private final Deque<Integer> epochs_ = new ArrayDeque<Integer>();
	
	public DurativeActionSimulator( final SimultaneousMoveSimulator<S, A> base_sim )
	{
		super( base_sim.nagents() );
		base_sim_ = base_sim;
		setTurn( base_sim_.turn() );
	}
	
	@Override
	public S state()
	{
		return base_sim_.state();
	}
	
	@Override
	public int nagents()
	{
		return base_sim_.nagents();
	}

	@Override
	public double[] reward()
	{
		return base_sim_.reward();
	}

	@Override
	public String detailString()
	{
		return base_sim_.detailString();
	}

	@Override
	public long horizon()
	{
		// FIXME: This should probably be ceil( ... )
		return base_sim_.horizon(); // FIXME: How do we implement this without a fixed epoch? / epoch_;
	}
	
	@Override
	public void setTurn( final int turn )
	{
		System.out.println( "[DurativeActionSimulator] setTurn( " + turn + " )" );
		super.setTurn( turn );
		base_sim_.setTurn( turn );
	}

	@Override
	protected void advance()
	{
		final int epoch = action_history_.peek().get( 0 ).T_;
		// TODO: Debugging
		for( final DurativeAction<S> ai : action_history_.peek() ) {
			if( ai.T_ != epoch ) {
				System.out.println( "ai.T_ = " + ai.T_ + "; epoch = " + epoch );
			}
			assert( ai.T_ == epoch );
		}
		epochs_.push( epoch );
		
		for( int t = 0; t < epoch; ++t ) {
			for( final DurativeAction<S, A> ai : action_history_.peek() ) {
				final DurativeAction<S, A> cp = ai.create();
				cp.policy_.setState( state(), base_sim_.depth() );
				final A policy_action = cp.policy_.getAction();
//					System.out.println( policy_action );
				base_sim_.takeAction( policy_action );
				final double r = base_sim_.reward();
				cp.policy_.actionResult( state(), r );
			}
		}
	}

	@Override
	protected void unadvance()
	{
		final int epoch = epochs_.pop();
		
		for( int t = 0; t < epoch; ++t ) {
			for( int i = 0; i < action_history_.peek().size(); ++i ) {
				base_sim_.untakeLastAction();
			}
		}
	}
	
	@Override
	public String toString()
	{
		// XXX: Other code depends on the format of toString().
		// Don't change for now!
		return "[d: " + base_sim_.depth() + ", p: " + turn() + "]";
	}

	@Override
	public boolean isTerminalState( )
	{
		return base_sim_.isTerminalState( );
	}
}
