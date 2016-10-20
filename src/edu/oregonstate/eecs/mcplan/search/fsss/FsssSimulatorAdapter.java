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

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.Simulator;

/**
 * @author jhostetler
 *
 */
public class FsssSimulatorAdapter<S extends State, A extends VirtualConstructor<A>> implements Simulator<S, A>
{
	private final FsssModel<S, A> model;
	private S s;
	private double r;
	
	private long t = 0;
	
	public FsssSimulatorAdapter( final FsssModel<S, A> model, final S s0 )
	{
		this.model = model;
		s = s0;
		r = model.reward( s0 );
	}
	
	@Override
	public S state()
	{ return s; }

	@Override
	public void takeAction( final JointAction<A> j )
	{
		assert( j.size() == 1 );
		final A a = j.get( 0 );
		r = model.reward( s, a );
		s = model.sampleTransition( s, a );
		r += model.reward( s );
		t += 1;
	}

	@Override
	public long depth()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long t()
	{
		return t;
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
		return new double[] { r };
	}

	@Override
	public boolean isTerminalState()
	{
		return s.isTerminal();
	}

	@Override
	public long horizon()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String detailString()
	{ return "FsssSimulatorAdapter"; }
	
}