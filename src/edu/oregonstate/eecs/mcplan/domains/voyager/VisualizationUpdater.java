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
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.sim.EpisodeListener;

/**
 * @author jhostetler
 *
 */
public class VisualizationUpdater implements
		EpisodeListener<VoyagerState, VoyagerEvent>
{

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveListener#startState(java.lang.Object, java.util.ArrayList)
	 */
	@Override
	public <P extends Policy<VoyagerState, VoyagerEvent>> void startState(
			final VoyagerState s, final ArrayList<P> policies )
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveListener#preGetAction(int)
	 */
	@Override
	public void preGetAction( final int player )
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveListener#postGetAction(int, java.lang.Object)
	 */
	@Override
	public void postGetAction( final int player, final VoyagerEvent action )
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveListener#onActionsTaken(java.lang.Object)
	 */
	@Override
	public void onActionsTaken( final VoyagerState sprime )
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveListener#endState(java.lang.Object)
	 */
	@Override
	public void endState( final VoyagerState s )
	{
		// TODO Auto-generated method stub

	}

}
