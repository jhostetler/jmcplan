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
public class ResetAdapter<S, A extends VirtualConstructor<A>>
	implements ResetSimulator<S, A>, UndoSimulator<S, A>
{
	public static <S, A extends VirtualConstructor<A>> ResetAdapter<S, A> of( final UndoSimulator<S, A> sim )
	{
		return new ResetAdapter<S, A>( sim );
	}
	
	private final UndoSimulator<S, A> sim_;
	private final long d_;
	
	public ResetAdapter( final UndoSimulator<S, A> sim )
	{
		sim_ = sim;
		d_ = sim_.depth();
	}
	
	@Override
	public S state()
	{ return sim_.state(); }

	@Override
	public void takeAction( final JointAction<A> a )
	{ sim_.takeAction( a ); }

	@Override
	public long depth()
	{ return sim_.depth(); }

	@Override
	public long t()
	{ return sim_.t(); }

	@Override
	public int nagents()
	{ return sim_.nagents(); }

	@Override
	public int[] turn()
	{ return sim_.turn(); }

	@Override
	public double[] reward()
	{ return sim_.reward(); }

	@Override
	public boolean isTerminalState()
	{ return sim_.isTerminalState(); }

	@Override
	public long horizon()
	{ return sim_.horizon(); }

	@Override
	public String detailString()
	{ return "ResetAdapter[" + sim_.detailString() + "]"; }

	@Override
	public void untakeLastAction()
	{ sim_.untakeLastAction(); }

	@Override
	public void reset()
	{
		while( sim_.depth() > d_ ) {
//			System.out.println( sim_.depth() );
			sim_.untakeLastAction();
		}
	}
}
