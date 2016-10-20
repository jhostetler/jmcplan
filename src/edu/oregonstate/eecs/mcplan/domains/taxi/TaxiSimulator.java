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
package edu.oregonstate.eecs.mcplan.domains.taxi;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class TaxiSimulator implements UndoSimulator<TaxiState, TaxiAction>
{
	private class StepAction extends TaxiAction
	{
		private int[][] old_taxis_ = null;
		
		@Override
		public void undoAction( final TaxiState s )
		{
			assert( old_taxis_ != null );
			for( int i = 0; i < s.other_taxis.length; ++i ) {
				Fn.memcpy( s.other_taxis[i], old_taxis_[i], old_taxis_[i].length );
			}
			
			old_taxis_ = null;
		}

		@Override
		public void doAction( final RandomGenerator rng_unused, final TaxiState s )
		{
			assert( old_taxis_ == null );
			old_taxis_ = Fn.copy( s.other_taxis );
			
			final int[] rs = Fn.range( 0, 5 );
			for( int i = 0; i < s.other_taxis.length; ++i ) {
				Fn.shuffle( rng_, rs );
				final int[] new_pos = new int[2];
				for( final int r : rs ) {
					Fn.memcpy( new_pos, s.other_taxis[i] );
					switch( r ) {
						case 0: new_pos[0] -= 1; break;
						case 1: new_pos[0] += 1; break;
						case 2: new_pos[1] -= 1; break;
						case 3: new_pos[1] += 1; break;
						default: assert( r == 4 ); break;
					}
					if( s.isLegalMove( i, s.other_taxis[i], new_pos ) ) {
						s.other_taxis[i] = new_pos;
						break;
					}
				}
			}
		}
		
		@Override
		public boolean isDone()
		{
			return old_taxis_ != null;
		}

		@Override
		public TaxiAction create()
		{
			return new StepAction();
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final RandomGenerator rng_;
	private final TaxiState s_;
	private final double slip_;
	public final int T;
	
	private final Deque<TaxiAction> action_history_ = new ArrayDeque<TaxiAction>();
	private final int Nevents_ = 2;
	
	public TaxiSimulator( final RandomGenerator rng, final TaxiState s, final double slip, final int T )
	{
		rng_ = rng;
		s_ = s;
		slip_ = slip;
		this.T = T;
		
//		s.passenger = rng_.nextInt( s.locations.size() );
//		s.destination = rng_.nextInt( s.locations.size() );
//
//		s.taxi[0] = rng_.nextInt( s.width );
//		s.taxi[1] = rng_.nextInt( s.height );
//
//		for( int i = 0; i < s.Nother_taxis; ++i ) {
//			final int[] pos = new int[2];
//			do {
//				pos[0] = rng_.nextInt( s.width );
//				pos[1] = rng_.nextInt( s.height );
//			}
//			while( s.isOccupied( pos, i ) );
//			Fn.memcpy( s.other_taxis[i], pos, 2 );
//		}
	}
	
	@Override
	public TaxiState state()
	{
		return s_;
	}

	@Override
	public void takeAction( final JointAction<TaxiAction> a )
	{
		final TaxiAction a0 = a.get( 0 );
		a0.doAction( rng_, s_ );
		action_history_.push( a0 );
		
		final StepAction step = new StepAction();
		step.doAction( s_ );
		action_history_.push( step );
		
		s_.t += 1;
		assert( s_.t <= s_.T );
		assert( action_history_.size() % Nevents_ == 0 );
	}
	
	@Override
	public void untakeLastAction()
	{
		s_.t -= 1;
		assert( s_.t >= 0 );
		
		for( int i = 0; i < Nevents_; ++i ) {
			final TaxiAction a = action_history_.pop();
			a.undoAction( s_ );
		}
		
		assert( action_history_.size() % Nevents_ == 0 );
	}

	@Override
	public long depth()
	{
		return action_history_.size();
	}

	@Override
	public long t()
	{
		return action_history_.size() / Nevents_;
	}

	@Override
	public int nagents()
	{
		return 1;
	}

	@Override
	public int[] turn()
	{
		return new int[] { 0 };
	}

	@Override
	public double[] reward()
	{
		final double step_cost = -1.0;
		
		if( s_.illegal_pickup_dropoff ) {
			return new double[] { step_cost - 10 };
		}
		else if( s_.pickup_success ) {
			return new double[] { step_cost + 5 };
		}
		else if( s_.goal ) {
			return new double[] { step_cost + 20 };
		}
		else {
			return new double[] { step_cost };
		}
	}

	@Override
	public boolean isTerminalState()
	{
		return s_.isTerminal();
	}

	@Override
	public long horizon()
	{
		return Long.MAX_VALUE;
	}

	@Override
	public String detailString()
	{
		return "TaxiSimulator";
	}

}
