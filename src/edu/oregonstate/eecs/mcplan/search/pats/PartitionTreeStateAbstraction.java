/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.pats;

import edu.oregonstate.eecs.mcplan.Representation;

/**
 * @author jhostetler
 *
 */
public class PartitionTreeStateAbstraction<S> implements StateAbstraction<S>
{
	public static class EquivalenceClass<S> extends Representation<S>
	{
		private final PartitionTreeEquivalenceRelation.LeafNode<S> leaf;
		
		private EquivalenceClass( final PartitionTreeEquivalenceRelation.LeafNode<S> leaf )
		{ this.leaf = leaf; }

		// -------------------------------------------------------------------
		// Representation<T>: LeafNode is a reference type
		
		@Override
		public Representation<S> copy()
		{
			return new EquivalenceClass<>( leaf );
		}

		@Override
		public boolean equals( final Object obj )
		{
			if( !(obj instanceof EquivalenceClass<?>) ) {
				return false;
			}
			@SuppressWarnings( "unchecked" )
			final EquivalenceClass<S> that = (EquivalenceClass<S>) obj;
			return leaf == that.leaf;
		}

		@Override
		public int hashCode()
		{ return leaf.hashCode(); }
	}
	
	// -----------------------------------------------------------------------
	
	private final PartitionTreeEquivalenceRelation<S> tree = new PartitionTreeEquivalenceRelation<>();

	@Override
	public Representation<S> encode( final S s )
	{
		final PartitionTreeEquivalenceRelation.LeafNode<S> leaf = tree.getPart( s );
		return new EquivalenceClass<>( leaf );
	}

	@Override
	public void refine()
	{
		// TODO Auto-generated method stub
		
	}
}
