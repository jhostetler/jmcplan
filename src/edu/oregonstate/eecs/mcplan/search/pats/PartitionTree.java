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

import java.util.ArrayList;
import java.util.Collections;

import com.google.common.collect.ImmutableList;

/**
 * @author jhostetler
 *
 */
public class PartitionTree<T>
{
	public static interface Node<T>
	{
		public abstract boolean isLeaf();
		public Node<T> successor( final T t );
		public Iterable<Node<T>> successors();
	}
	
	public static class LeafNode<T> implements Node<T>
	{
		private final SplitNode<T> parent;
		private final int part;
		private final ImmutableList<T> elements;
		
		private LeafNode( final SplitNode<T> parent, final int part, final Iterable<T> elements )
		{
			this.parent = parent;
			this.part = part;
			this.elements = ImmutableList.copyOf( elements );
		}
		
		public ImmutableList<T> elements()
		{
			return elements;
		}
		
		private SplitNode<T> split( final Classifier<T> classifier )
		{
			return new SplitNode<>( classifier, elements );
		}
		
		@Override
		public boolean isLeaf()
		{ return true; }

		@Override
		public Node<T> successor( final T t )
		{ return null; }

		@Override
		public Iterable<Node<T>> successors()
		{ return Collections.emptyList(); }
	}
	
	public static class SplitNode<T> implements Node<T>
	{
		private final Classifier<T> classifier;
		private final ArrayList<Node<T>> successors;
		
		private SplitNode()
		{
			classifier = null;
			successors = new ArrayList<>();
		}
		
		private SplitNode( final Classifier<T> classifier, final Iterable<T> elements )
		{
			this.classifier = classifier;
			successors = new ArrayList<>( classifier.Nclasses() );
			final ArrayList<ArrayList<T>> parts = new ArrayList<>( classifier.Nclasses() );
			for( int i = 0; i < classifier.Nclasses(); ++i ) {
				parts.add( new ArrayList<T>() );
			}
			for( final T x : elements ) {
				final int y = classifier.classify( x );
				parts.get( y ).add( x );
			}
			for( int i = 0; i < classifier.Nclasses(); ++i ) {
				successors.add( new LeafNode<T>( this, i, parts.get( i ) ) );
			}
		}
		
		@Override
		public boolean isLeaf()
		{
			return false;
		}
		
		@Override
		public Node<T> successor( final T t )
		{
			return successors.get( classifier.classify( t ) );
		}

		@Override
		public Iterable<Node<T>> successors()
		{
			return successors;
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final SplitNode<T> root;
	
	private final ArrayList<LeafNode<T>> leaf_nodes = new ArrayList<>();
	
	public PartitionTree( final Iterable<T> elements )
	{
		this.root = new SplitNode<>();
		final LeafNode<T> leaf = new LeafNode<>( root, 0, elements );
		this.root.successors.add( leaf );
		leaf_nodes.add( leaf );
	}
	
	public void refine( final LeafNode<T> leaf, final Classifier<T> classifier )
	{
		final SplitNode<T> split = leaf.split( classifier );
		leaf.parent.successors.set( leaf.part, split );
		leaf_nodes.remove( leaf );
		for( final Node<T> new_leaf : split.successors() ) {
			assert( new_leaf.isLeaf() );
			leaf_nodes.add( (LeafNode<T>) new_leaf );
		}
	}
	
	public LeafNode<T> getPart( final T t )
	{
		SplitNode<T> n = root;
		while( true ) {
			final Node<T> nprime = n.successor( t );
			if( nprime.isLeaf() ) {
				return (LeafNode<T>) nprime;
			}
			else {
				n = (SplitNode<T>) nprime;
			}
		}
	}
}
