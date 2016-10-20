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
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;



/**
 * A stub implementation of MctsVisitor. Use this when you need a visitor that
 * does nothing, or inherit from it if you only need to override one or
 * two methods.
 * 
 * @author jhostetler
 */
public class DefaultMctsVisitor<S, A extends VirtualConstructor<A>>
	implements MctsVisitor<S, A>
{
	@Override
	public void startEpisode( final S s, final int nagents, final int[] turn )
	{ }

	@Override
	public boolean startRollout( final S s, final int[] turn )
	{ return true; }

	@Override
	public void startTree( final S s, final int[] turn )
	{ }

	@Override
	public void treeAction( final JointAction<A> a, final S sprime, final int[] next_turn )
	{ }

	@Override
	public void treeDepthLimit( final S s, final int[] turn )
	{ }

	@Override
	public void startDefault( final S s, final int[] turn )
	{ }

	@Override
	public void defaultAction( final JointAction<A> a, final S sprime, final int[] next_turn )
	{ }

	@Override
	public void defaultDepthLimit( final S s, final int[] turn )
	{ }

	@Override
	public void depthLimit( final S s, final int[] turn )
	{ }

	@Override
	public void checkpoint()
	{ }

	@Override
	public boolean halt()
	{
		return false;
	}
}
