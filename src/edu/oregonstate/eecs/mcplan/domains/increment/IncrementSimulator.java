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
	public int getNumAgents()
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
	public double getReward()
	{
		return reward_history_.peek();
	}

	@Override
	public boolean isTerminalState( final IncrementState s )
	{
		// TODO Auto-generated method stub
		return false;
	}

}
