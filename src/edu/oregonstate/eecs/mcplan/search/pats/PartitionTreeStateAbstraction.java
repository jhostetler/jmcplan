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
