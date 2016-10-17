/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.pats;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author jhostetler
 *
 */
public class PartitionTreeEquivalenceRelation<T>
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
		
		private LeafNode( final SplitNode<T> parent, final int part )
		{
			this.parent = parent;
			this.part = part;
		}
		
		private SplitNode<T> split( final Classifier<T> classifier )
		{
			return new SplitNode<>( classifier );
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
		
		private SplitNode( final Classifier<T> classifier )
		{
			this.classifier = classifier;
			successors = new ArrayList<>( classifier.Nclasses() );
			for( int i = 0; i < classifier.Nclasses(); ++i ) {
				successors.add( new LeafNode<T>( this, i ) );
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
	
	public PartitionTreeEquivalenceRelation()
	{
		this.root = new SplitNode<>();
		final LeafNode<T> leaf = new LeafNode<>( root, 0 );
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
