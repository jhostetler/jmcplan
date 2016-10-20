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
package edu.oregonstate.eecs.mcplan.domains.fuelworld;

import java.util.ArrayDeque;
import java.util.Deque;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;

/**
 * @author jhostetler
 *
 */
public class FuelWorldSimulator implements UndoSimulator<FuelWorldState, FuelWorldAction>
{
	private final FuelWorldState s_;
	
	private final Deque<FuelWorldAction> action_history_ = new ArrayDeque<FuelWorldAction>();
	
	public FuelWorldSimulator( final FuelWorldState s )
	{
		s_ = s;
	}
	
	@Override
	public FuelWorldState state()
	{
		return s_;
	}

	@Override
	public void takeAction( final JointAction<FuelWorldAction> a )
	{
		final FuelWorldAction a0 = a.get( 0 );
		a0.doAction( s_ );
		action_history_.push( a0 );
		
		s_.t += 1;
		assert( s_.t <= s_.T );
	}
	
	@Override
	public void untakeLastAction()
	{
		s_.t -= 1;
		assert( s_.t >= 0 );
		
		final FuelWorldAction a = action_history_.pop();
		a.undoAction( s_ );
	}

	@Override
	public long depth()
	{
		return action_history_.size();
	}

	@Override
	public long t()
	{
		return action_history_.size();
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
		if( s_.location == s_.goal ) {
			return new double[] { -1 + 20 }; //0 };
//			return new double[] { 0 };
		}
		else if( s_.out_of_fuel && s_.fuel == 0 ) {
			return new double[] { -1 + -100 }; //-20 - (s_.T - s_.t) };
//			return new double[] { -20 - (s_.T - s_.t) };
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
		return s_.T - s_.t;
	}

	@Override
	public String detailString()
	{
		return "FuelWorldSimulator";
	}
}
