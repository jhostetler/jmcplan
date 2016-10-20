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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class RandomPartitionRepresenter<S extends State, A extends VirtualConstructor<A>>
	extends ClassifierRepresenter<S, A>
{
	public static class Abstraction<S extends State, A extends VirtualConstructor<A>>
		extends FsssAbstraction<S, A>
	{
		private final FsssModel<S, A> model;
		private final int k;
	
		public Abstraction( final FsssModel<S, A> model, final int k )
		{
			this.model = model;
			this.k = k;
		}
		
		@Override
		public String toString()
		{
			return "RandomPartition(" + k + ")";
		}
		
		@Override
		public ClassifierRepresenter<S, A> createRepresenter()
		{
			return new RandomPartitionRepresenter<S, A>( model, this, k );
		}
	}
	
	private static class MapSplitNode<S extends State, A extends VirtualConstructor<A>>
		extends SplitNode<S, A>
	{
		final RandomGenerator rng;
		public final ArrayList<DataNode<S, A>> nodes = new ArrayList<DataNode<S, A>>();
		
		public Map<Representation<S>, DataNode<S, A>> assignments = new LinkedHashMap<Representation<S>, DataNode<S, A>>();
		
		public MapSplitNode( final RandomGenerator rng, final int k, final DataNode.Factory<S, A> f )
		{
			this.rng = rng;
			for( int i = 0; i < k; ++i ) {
				this.nodes.add( f.createDataNode() );
			}
		}
		
		@Override
		public SplitNode<S, A> create( final DataNode.Factory<S, A> f )
		{
			return new MapSplitNode<S, A>( rng, nodes.size(), f );
		}
		
		@Override
		public void addGroundStateNode( final FsssStateNode<S, A> gsn )
		{
//			assert( false );
//			DataNode<S, A> dn = child( gsn.x() );
//			if( dn == null ) {
//				final int i = rng.nextInt( nodes.size() );
//				dn = nodes.get( i );
//				assignments.put( gsn.x(), dn );
//			}
//			dn.aggregate.addGroundStateNode( gsn );
			
			throw new UnsupportedOperationException();
		}
		
		@Override
		public DataNode<S, A> child( final FactoredRepresentation<S> x )
		{
			DataNode<S, A> dn = assignments.get( x );
			if( dn == null ) {
				final int i = rng.nextInt( nodes.size() );
				dn = nodes.get( i );
				assignments.put( x, dn );
			}
			return dn;
			
//			return assignments.get( x );
		}

		@Override
		public Generator<? extends DataNode<S, A>> children()
		{
			return Generator.fromIterator( nodes.iterator() );
		}
	}
	
	// -----------------------------------------------------------------------

	private static class MyDataNodeFactory<S extends State, A extends VirtualConstructor<A>>
		extends DataNode.Factory<S, A>
	{
		private final RandomGenerator rng;
		private final int k;
		private final DataNode.Factory<S, A> base_dnf = new DataNode.DefaultFactory<S, A>();
		
		public MyDataNodeFactory( final RandomGenerator rng, final int k )
		{
			this.rng = rng;
			this.k = k;
		}
		
		@Override
		public DataNode<S, A> createDataNode()
		{
			final DataNode<S, A> dn = base_dnf.createDataNode();
			dn.split = new MapSplitNode<S, A>( rng, k, base_dnf );
			return dn;
		}
	}
	
	// -----------------------------------------------------------------------
	
	public final int k;
	
	public RandomPartitionRepresenter( final FsssModel<S, A> model, final FsssAbstraction<S, A> abstraction,
									   final int k )
	{
		super( model, abstraction, new MyDataNodeFactory<S, A>( model.rng(), k ) );
		this.k = k;
	}
	
	@Override
	public RandomPartitionRepresenter<S, A> create()
	{
		return new RandomPartitionRepresenter<S, A>( model, abstraction, k );
	}
}
