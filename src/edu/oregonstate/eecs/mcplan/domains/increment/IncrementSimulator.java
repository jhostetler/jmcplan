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
package edu.oregonstate.eecs.mcplan.domains.increment;

import java.util.ArrayDeque;
import java.util.Deque;

import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;

/**
 * Implements a simulator for the Increment Game.
 * 
 * In the Increment Game there are k integer counters that begin at 0. There
 * is also a scoring limit L that is fixed for the game. Player 0's actions
 * are to decrement any single counter by 1, and Player 1's actions are to
 * increment a single counter by 1. If a counter reaches +/- L, it is reset
 * to 0 and the appropriate player receives 1 point.
 * 
 * The game has a minimax value of 0. If k = 1, achieving it will require
 * a mixed strategy, otherwise a "follow the leader" strategy will ensure 0
 * cost.
 * 
 * The point of this game is to create a simple scenario where there is a
 * "critical decision frequency", such that if decisions are not made at at
 * least the critical frequency, the minimax outcome cannot be achieved.
 * 
 * @author jhostetler
 *
 */
public class IncrementSimulator extends SimultaneousMoveSimulator<IncrementState, IncrementEvent>
{
	private final IncrementState state_;
	public final int limit;
	private final long horizon_;
	private long t_ = 0L;
	private final Deque<Integer> reward_history_ = new ArrayDeque<Integer>();
	
	public IncrementSimulator( final int Ncounters, final int limit, final long horizon )
	{
		state_ = new IncrementState( Ncounters );
		this.limit = limit;
		horizon_ = horizon;
		reward_history_.push( 0 );
	}
	
	@Override
	public int nagents()
	{
		return 2;
	}

	@Override
	public long horizon()
	{
		return horizon_ - t_;
	}

	@Override
	public IncrementState state()
	{
		return state_;
	}

	@Override
	protected void advance()
	{
		int reward = 0;
		for( int i = 0; i < state_.counters.length; ++i ) {
			if( state_.counters[i] <= -limit ) {
				applyEvent( new IncrementReset( 0, i, limit ) );
				reward = -1;
			}
			else if( state_.counters[i] >= limit ) {
				applyEvent( new IncrementReset( 1, i, limit ) );
				reward = 1;
			}
		}
		t_ += 1;
		reward_history_.push( reward );
	}

	@Override
	protected void unadvance()
	{
		t_ -= 1;
		reward_history_.pop();
	}

	@Override
	public double[] reward()
	{
		return new double[] { reward_history_.peek(), -reward_history_.peek() };
	}

	@Override
	public boolean isTerminalState( )
	{
		// TODO Auto-generated method stub
		return false;
	}

}
