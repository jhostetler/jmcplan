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
package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * Sample budget.
 */
public class FsssSampleBudget<S extends State, A extends VirtualConstructor<A>> implements Budget
{
	private final FsssModel<S, A> model;
	private final int budget;
	
	private boolean exceeded = false;
	private int exceeded_at = 0;
	
	public FsssSampleBudget( final FsssModel<S, A> model, final int budget )
	{
		this.model = model;
		this.budget = budget;
	}
	
	@Override
	public boolean isExceeded()
	{
		if( exceeded ) {
			return true;
		}
		final int count = model.sampleCount();
		if( count >= budget ) {
			exceeded_at = count;
			exceeded = true;
		}
		return exceeded;
	}

	@Override
	public void reset()
	{
		model.resetSampleCount();
		exceeded = false;
		exceeded_at = 0;
	}
	
	@Override
	public String toString()
	{
		return "SampleBudget[" + budget + "]";
	}

	@Override
	public double actualDouble()
	{
		return exceeded_at;
	}
}
