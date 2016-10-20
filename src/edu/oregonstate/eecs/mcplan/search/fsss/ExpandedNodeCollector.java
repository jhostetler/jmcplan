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

package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * Collects all ASNs that were Expanded during a complete run of FSSS.
 * 
 * @author jhostetler
 *
 * @param <S>
 * @param <A>
 */
public final class ExpandedNodeCollector<S extends State, A extends VirtualConstructor<A>>
		implements AbstractFsss.Listener<S, A>
{
	private FsssAbstractActionNode<S, A> root_action = null;
	public final Map<FsssAbstractActionNode<S, A>, ArrayList<FsssAbstractStateNode<S, A>>>
		expanded = new HashMap<FsssAbstractActionNode<S, A>, ArrayList<FsssAbstractStateNode<S, A>>>();
	
	@Override
	public void onVisit( final FsssAbstractStateNode<S, A> asn )
	{ }

	@Override
	public void onExpand( final FsssAbstractStateNode<S, A> asn )
	{
		assert( root_action != null );
		ArrayList<FsssAbstractStateNode<S, A>> subtree = expanded.get( root_action );
		if( subtree == null ) {
			subtree = new ArrayList<FsssAbstractStateNode<S, A>>();
			expanded.put( root_action, subtree );
		}
		subtree.add( asn );
	}

	@Override
	public void onLeaf( final FsssAbstractStateNode<S, A> asn )
	{ }

	@Override
	public void onTrajectoryStart()
	{
		root_action = null;
	}

	@Override
	public void onActionChoice( final FsssAbstractActionNode<S, A> aan )
	{
		if( root_action == null ) {
			root_action = aan;
		}
	}

	@Override
	public void onStateChoice( final FsssAbstractStateNode<S, A> asn )
	{ }

	@Override
	public void onTrajectoryEnd()
	{ }
}
