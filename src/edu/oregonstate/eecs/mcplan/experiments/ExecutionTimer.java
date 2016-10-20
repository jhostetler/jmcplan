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
package edu.oregonstate.eecs.mcplan.experiments;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.EpisodeListener;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class ExecutionTimer<S, A extends VirtualConstructor<A>> implements EpisodeListener<S, A>
{
	private final MeanVarianceAccumulator mv_ = new MeanVarianceAccumulator();
	private final MedianAccumulator median_ = new MedianAccumulator();
	private long start_time_ = 0L;
	private final boolean disabled_ = false;

	public MeanVarianceAccumulator meanVariance()
	{
		return mv_;
	}
	
	public MedianAccumulator median()
	{
		return median_;
	}

	@Override
	public <P extends Policy<S, JointAction<A>>> void startState( final S s, final P pi )
	{ }
	
	@Override
	public void preGetAction()
	{
		start_time_ = System.currentTimeMillis();
		System.out.println( "*** start_time = " + start_time_ );
	}

	@Override
	public void postGetAction( final JointAction<A> action )
	{
		final long tdiff = System.currentTimeMillis() - start_time_;
		System.out.println( "*** Elapsed time = " + tdiff );
		mv_.add( tdiff );
		median_.add( tdiff );
	}

	@Override
	public void onActionsTaken( final S sprime )
	{ }

	@Override
	public void endState( final S s )
	{ }
}
