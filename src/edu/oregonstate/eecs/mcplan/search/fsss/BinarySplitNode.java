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

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Generator;

public class BinarySplitNode<S extends State, A extends VirtualConstructor<A>> extends SplitNode<S, A>
{
	public final int attribute;
	public final double threshold;
	
	public final DataNode<S, A> left;
	public final DataNode<S, A> right;
	
	public BinarySplitNode( final DataNode.Factory<S, A> f, final int attribute, final double threshold )
	{
		this.left = f.createDataNode();
		this.right = f.createDataNode();
		this.attribute = attribute;
		this.threshold = threshold;
	}
	
	@Override
	public SplitNode<S, A> create( final DataNode.Factory<S, A> f )
	{
		return new BinarySplitNode<S, A>( f, attribute, threshold );
	}
	
	@Override
	public void addGroundStateNode( final FsssStateNode<S, A> gsn )
	{
		child( gsn.x() ).aggregate.addGroundStateNode( gsn );
	}
	
	@Override
	public DataNode<S, A> child( final FactoredRepresentation<S> x )
	{
		if( x.phi()[attribute] < threshold ) {
			return left;
		}
		else {
			return right;
		}
	}

	@Override
	public Generator<? extends DataNode<S, A>> children()
	{
		return new Generator<DataNode<S, A>>() {
			int i = 0;
			
			@Override
			public boolean hasNext()
			{ return i < 2; }

			@Override
			public DataNode<S, A> next()
			{
				switch( i++ ) {
				case 0: return left;
				case 1: return right;
				default: throw new IllegalStateException( "hasNext() == false" );
				}
			}
		};
	}
	
	@Override
	public String toString()
	{
		return attribute + "@" + threshold;
	}
}
