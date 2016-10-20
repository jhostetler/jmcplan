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

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * A node in an abstraction tree.
 * @param <S>
 * @param <A>
 */
public class DataNode<S extends State, A extends VirtualConstructor<A>>
{
	public static abstract class Factory<S extends State, A extends VirtualConstructor<A>>
	{
		public abstract DataNode<S, A> createDataNode();
	}

	public static class DefaultFactory<S extends State, A extends VirtualConstructor<A>>
		extends DataNode.Factory<S, A>
	{
		private int next_id = 0;
		
		@Override
		public DataNode<S, A> createDataNode()
		{
			return new DataNode<S, A>( next_id++ );
		}
	}
	
	// -----------------------------------------------------------------------

	/**
	 * If this node is an internal node, stores the splitting criterion.
	 */
	public SplitNode<S, A> split = null;
	
	/**
	 * If this node is a leaf, stores the corresponding element of the state
	 * space partition.
	 */
	public FsssAbstractStateNode<S, A> aggregate = null;
	
	/**
	 * Integer id unique within the tree.
	 */
	public final int id;
	
	public DataNode( final int id )
	{
		this.id = id;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "DataNode@" );
		sb.append( Integer.toHexString( System.identityHashCode( this ) ) );
		sb.append( " -> " );
		if( split != null ) {
			sb.append( split );
		}
		else if( aggregate != null ) {
			sb.append( aggregate );
		}
		else {
			sb.append( "null" );
		}
		return sb.toString();
	}
}
