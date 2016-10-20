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

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * Right about here is where I give up. This class tries to force static
 * abstractions into the ClassifierRepresenter interface by any means
 * necessary. Proceed with great caution!
 * 
 * @author jhostetler
 */
public class StaticClassifierRepresenter<S extends State, A extends VirtualConstructor<A>>
	extends ClassifierRepresenter<S, A>
{
	private static final ch.qos.logback.classic.Logger Log = LoggerManager.getLogger( "log.search" );
	
	public StaticClassifierRepresenter( final FsssModel<S, A> model,
			final FsssAbstraction<S, A> abstraction )
	{
		super( model, abstraction );
	}

	@Override
	protected DataNode<S, A> novelInstance( final DataNode<S, A> dt_root, final FactoredRepresentation<S> x )
	{
		Log.trace( "\tnovelInstance(): {}", x );
		final DataNode<S, A> dn = ((MapSplitNode<S, A>) dt_root.split).createChild( x );
		// This is where all of the DNs that contain aggregates are created.
		// dt_leaves is never modified externally because we're not doing refinements.
		dt_leaves.add( dn );
		return dn;
	}

	@Override
	public ClassifierRepresenter<S, A> create()
	{
		return new StaticClassifierRepresenter<S, A>( model, abstraction );
	}
	
	@Override
	public ClassifierRepresenter<S, A> emptyInstance()
	{
		// In the static case the new instance is truly empty. It will be
		// created on the fly by the search algorithm.
		return create();
	}
	
	@Override
	protected DataNode<S, A> requireActionSet( final S s )
	{
		final Representation<S> arep = model.action_repr().encode( s );
		DataNode<S, A> dt_root = dt_roots.get( arep );
		if( dt_root == null ) {
			dt_root = dn_factory.createDataNode();
			dt_roots.put( arep, dt_root );
			
			// With a static abstraction, the root is always a split node
			// with one layer of children corresponding to the different
			// equivalence classes under the abstraction.
			dt_root.split = new MapSplitNode<S, A>( dn_factory );
			
//			dt_leaves.add( dt_root );
		}
		
		return dt_root;
	}
}
