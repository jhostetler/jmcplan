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

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class MultiEpisodeSimulator<S, A extends VirtualConstructor<A>> implements Simulator<S, A>
{

	@Override
	public S state()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void takeAction( final JointAction<A> a )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public long depth()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long t()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int nagents()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] turn()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] reward()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isTerminalState()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long horizon()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String detailString()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
