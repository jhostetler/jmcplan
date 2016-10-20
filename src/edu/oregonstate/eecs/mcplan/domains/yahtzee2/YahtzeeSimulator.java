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
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

/**
 * @author jhostetler
 *
 */
public class YahtzeeSimulator implements UndoSimulator<YahtzeeState, YahtzeeAction>
{
	private final RandomGenerator rng_;
	private final YahtzeeState s_;
	
	private final int Horizon_ = YahtzeeScores.values().length;
	
	private final Deque<JointAction<YahtzeeAction>> action_history_
		= new ArrayDeque<JointAction<YahtzeeAction>>();
	
	private final TIntStack scores_ = new TIntArrayStack();
	
	public YahtzeeSimulator( final RandomGenerator rng, final YahtzeeState s )
	{
		rng_ = rng;
		s_ = s;
		scores_.push( s_.score() );
	}
	
	@Override
	public YahtzeeState state()
	{
		return s_;
	}

	@Override
	public void takeAction( final JointAction<YahtzeeAction> a )
	{
		assert( a.nagents == 1 );
		scores_.push( s_.score() );
		a.get( 0 ).doAction( rng_, s_ );
		action_history_.push( a );
	}

	@Override
	public void untakeLastAction()
	{
		final JointAction<YahtzeeAction> a = action_history_.pop();
		a.get( 0 ).undoAction( s_ );
		scores_.pop();
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
		return new double[] { s_.score() - scores_.peek() };
		
//		if( s_.isTerminal() ) {
//			return new double[] { s_.score() };
//		}
//		else {
//			return new double[] { 0 };
//		}
	}

	@Override
	public boolean isTerminalState()
	{
		return s_.isTerminal();
	}

	@Override
	public long horizon()
	{
		return Horizon_ - depth();
	}

	@Override
	public String detailString()
	{
		return "YahtzeeSimulator";
	}

}
