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
package edu.oregonstate.eecs.mcplan.search.pats;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jhostetler
 *
 */
public class SampleStateNode<S, A>
{
	public final S s;
	private final Map<A, SampleActionNode<S, A>> successors = new LinkedHashMap<>();
	
	private final int multiplicity = 0;
	
	private double U;
	private double L;
	public final double r;
	public final int depth;
	
	public SampleStateNode( final SampleActionNode<S, A> predecessor, final S s, final double r )
	{
		this.s = s;
		this.r = r;
		this.depth = predecessor.depth - 1;
	}
	
	public void addSuccessor( final SampleActionNode<S, A> an )
	{
		final SampleActionNode<S, A> prev = successors.put( an.a, an );
		assert( prev == null );
	}
	
	public SampleActionNode<S, A> successor( final A a )
	{ return actions.get( a ); }
}
