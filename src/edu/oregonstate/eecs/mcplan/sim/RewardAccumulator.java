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

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class RewardAccumulator<S, A extends VirtualConstructor<A>> implements EpisodeListener<S, A>
{
	public final double discount;
	private double running_discount_ = 1.0;
	private final double[] v_;
	private int steps_ = 0;
	
	public RewardAccumulator( final int nagents, final double discount )
	{
		this.discount = discount;
		v_ = new double[nagents];
	}
	
	public double[] v()
	{
		return Arrays.copyOf( v_, v_.length );
	}
	
	public int steps()
	{
		return steps_;
	}
	
	@Override
	public <P extends Policy<S, JointAction<A>>> void startState( final S s,
			final double[] r, final P pi )
	{
		Fn.vplus_inplace( v_, r );
		running_discount_ *= discount;
//		System.out.println( "start" );
	}

	@Override
	public void preGetAction()
	{ }

	@Override
	public void postGetAction( final JointAction<A> a )
	{ }

	@Override
	public void onActionsTaken( final S sprime, final double[] r )
	{
		Fn.vplus_ax_inplace( v_, running_discount_, r );
		running_discount_ *= discount;
		steps_ += 1;
//		System.out.println( "step" );
	}

	@Override
	public void endState( final S s )
	{ }
}
