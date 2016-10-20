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


/**
 * @author jhostetler
 *
 */
public class DefaultMctsNegamaxVisitor<S, A> implements MctsNegamaxVisitor<S, A>
{
	@Override
	public void startEpisode( final S s )
	{ }
	
	@Override
	public boolean startRollout( final S s )
	{ return true; }
	
	@Override
	public void startTree( final S s )
	{ }

	@Override
	public void treeAction( final A a, final S sprime )
	{ }

	@Override
	public void treeDepthLimit( final S s )
	{ }
	
	@Override
	public void startDefault( final S s )
	{ }

	@Override
	public void defaultAction( final A a, final S sprime )
	{ }

	@Override
	public void defaultDepthLimit( final S s )
	{ }

	@Override
	public void depthLimit( final S s )
	{ }

	@Override
	public double terminal( final S s )
	{
		return 0;
	}

	@Override
	public boolean isTerminal( final S s )
	{
		return false;
	}
}
