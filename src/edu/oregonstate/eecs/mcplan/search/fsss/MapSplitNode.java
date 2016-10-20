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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class MapSplitNode<S extends State, A extends VirtualConstructor<A>>
	extends SplitNode<S, A>
{
	// Needs to be a LinkedHashMap to ensure consistent iteration order over different runs.
	public Map<Representation<S>, DataNode<S, A>> assignments = new LinkedHashMap<Representation<S>, DataNode<S, A>>();
	
	private final DataNode.Factory<S, A> dn_factory;
	
	public MapSplitNode( final DataNode.Factory<S, A> f )
	{
		dn_factory = f;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "{MapSplit: " );
		for( final Map.Entry<Representation<S>, DataNode<S, A>> e : assignments.entrySet() ) {
			sb.append( e.getKey() ).append( " -> " )
			  .append( "@" ).append( Integer.toHexString( System.identityHashCode( e.getValue() ) ) );
		}
		sb.append( "}" );
		return sb.toString();
//		return "{MapSplit: " + assignments + "}";
	}
	
	/**
	 * FIXME: This function shouldn't be called "create", because you
	 * don't really want an empty one. You want a copy that doesn't contain
	 * the instances from the old one, but is behaviorally equivalent.
	 * @param f
	 * @return
	 */
	@Override
	public SplitNode<S, A> create( final DataNode.Factory<S, A> f )
	{
		final MapBinarySplitNode<S, A> copy = new MapBinarySplitNode<S, A>( f );
		return copy;
	}
	
	@Override
	public void addGroundStateNode( final FsssStateNode<S, A> gsn )
	{
		final DataNode<S, A> dn = child( gsn.x() );
		assert( dn != null );
//		if( dn == null ) {
//			dn = dn_factory.createDataNode();
//			assignments.put( gsn.x(), dn );
//		}
		dn.aggregate.addGroundStateNode( gsn );
	}
	
	public DataNode<S, A> createChild( final FactoredRepresentation<S> x )
	{
		final DataNode<S, A> dn = dn_factory.createDataNode();
		final DataNode<S, A> check = assignments.put( x, dn );
		assert( check == null );
		return dn;
	}
	
	@Override
	public DataNode<S, A> child( final FactoredRepresentation<S> x )
	{
		return assignments.get( x );
	}

	@Override
	public Generator<? extends DataNode<S, A>> children()
	{
		return new Generator<DataNode<S, A>>() {
			final Iterator<DataNode<S, A>> itr = assignments.values().iterator();
			
			@Override
			public boolean hasNext()
			{ return itr.hasNext(); }

			@Override
			public DataNode<S, A> next()
			{ return itr.next(); }
		};
	}
}
