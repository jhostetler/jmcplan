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
package edu.oregonstate.eecs.mcplan.domains.racegrid;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;

/**
 * @author jhostetler
 *
 */
public class RacegridSimulator implements UndoSimulator<RacegridState, RacegridAction>
{
	private class StepAction extends RacegridAction
	{
		private int old_x_ = 0;
		private int old_y_ = 0;
		private int old_dx_ = 0;
		private int old_dy_ = 0;
		private int old_ddx_ = 0;
		private int old_ddy_ = 0;
		private boolean old_crashed_ = false;
		private boolean old_goal_ = false;
		private int old_t_ = 0;
		
		private boolean done_ = false;
		
		@Override
		public void undoAction( final RacegridState s )
		{
			assert( done_ );
			s.x = old_x_;
			s.y = old_y_;
			s.dx = old_dx_;
			s.dy = old_dy_;
			s.ddx = old_ddx_;
			s.ddy = old_ddy_;
			s.crashed = old_crashed_;
			s.goal = old_goal_;
			s.t = old_t_;
			done_ = false;
		}

		@Override
		public void doAction( final RandomGenerator rng, final RacegridState s )
		{
			assert( !done_ );
//			System.out.println( "*** Step" );
			
			// Store state
			old_x_ = s.x;
			old_y_ = s.y;
			old_dx_ = s.dx;
			old_dy_ = s.dy;
			old_ddx_ = s.ddx;
			old_ddy_ = s.ddy;
			old_crashed_ = s.crashed;
			old_goal_ = s.goal;
			old_t_ = s.t;

			RacegridDynamics.applyDynamics( rng_, s, slip_ );
			
			done_ = true;
		}
		
		@Override
		public boolean isDone()
		{
			return done_;
		}

		@Override
		public RacegridAction create()
		{
			return new StepAction();
		}
	}
	
	
	
	// -----------------------------------------------------------------------
	
	private final RandomGenerator rng_;
	private final RacegridState s_;
	private final double slip_;
	
	private final Deque<RacegridAction> action_history_ = new ArrayDeque<RacegridAction>();
	private final int Nevents_ = 2;
	
	public RacegridSimulator( final RandomGenerator rng, final RacegridState s, final double slip )
	{
		rng_ = rng;
		s_ = s;
		slip_ = slip;
	}
	
	@Override
	public RacegridState state()
	{
		return s_;
	}

	@Override
	public void takeAction( final JointAction<RacegridAction> a )
	{
		final RacegridAction a0 = a.get( 0 );
		a0.doAction( null, s_ );
		action_history_.push( a0 );
		
		final StepAction step = new StepAction();
		step.doAction( null, s_ );
		action_history_.push( step );
		
//		s_.t += 1;
		assert( s_.t <= s_.T );
		assert( action_history_.size() % Nevents_ == 0 );
	}
	
	@Override
	public void untakeLastAction()
	{
//		s_.t -= 1;
		
		for( int i = 0; i < Nevents_; ++i ) {
			final RacegridAction a = action_history_.pop();
			a.undoAction( s_ );
		}
		
		assert( s_.t >= 0 );
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
		if( s_.crashed ) {
			return new double[] { -s_.T }; // { -(s_.T - s_.t) - 10 }; //{ -10 };
		}
		else if( s_.goal ) {
			return new double[] { 0 };
		}
		else {
			return new double[] { -1 };
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
		return "RacegridSimulator";
	}
	
}
