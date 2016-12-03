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

package edu.oregonstate.eecs.mcplan.sim;

import edu.oregonstate.eecs.mcplan.search.fsss.Budget;

/**
 * @author jhostetler
 *
 */
public class TransitionBudget<S, A> implements Budget, SimulationListener<S, A>
{
	private int transitions = 0;
	public final int budget;
	
	public TransitionBudget( final int budget )
	{
		this.budget = budget;
	}
	
	@Override
	public void onInitialStateSample( final StateNode<S, A> s0 )
	{ }
	
	@Override
	public void onTransitionSample( final ActionNode<S, A> trans )
	{
		transitions += 1;
		if( transitions < 0 ) {
			throw new ArithmeticException( "integer overflow" );
		}
	}

	@Override
	public boolean isExceeded()
	{
		return transitions >= budget;
	}

	@Override
	public double actualDouble()
	{
		return transitions;
	}

	@Override
	public void reset()
	{
		transitions = 0;
	}
}
