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

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * Epsilon greedy subtree chooser that *never* chooses the currently optimal
 * subtree. This makes is incomplete.
 * 
 * This is the ordering used in the experiments in the UAI 2015 submission.
 */
public class IncompleteEpsilonGreedyRefinementOrder<S extends State, A extends VirtualConstructor<A>>
	extends RefinementOrderBase<S, A>
{
	public static class Factory<S extends State, A extends VirtualConstructor<A>>
		implements RefinementOrderBase.Factory<S, A>
	{
		private final RandomGenerator rng;
		private final double epsilon;
		private final SubtreeRefinementOrder.Factory<S, A> subtree_factory;
		
		public Factory( final RandomGenerator rng, final double epsilon,
						final SubtreeRefinementOrder.Factory<S, A> subtree_factory )
		{
			this.rng = rng;
			this.epsilon = epsilon;
			this.subtree_factory = subtree_factory;
		}
		
		@Override
		public String toString()
		{
			return "IncompleteEpsilonGreedy(" + epsilon + "; " + subtree_factory + ")";
		}
	
		@Override
		public RefinementOrderBase<S, A> create( final FsssParameters parameters, final FsssModel<S, A> model,
									   final FsssAbstractStateNode<S, A> root )
		{
			final ArrayList<SubtreeRefinementOrder<S, A>> subtrees
				= new ArrayList<SubtreeRefinementOrder<S, A>>();
			for( final FsssAbstractActionNode<S, A> aan : root.successors() ) {
				subtrees.add( subtree_factory.create( parameters, model, aan ) );
			}
			assert( subtrees.size() > 1 );
			return new IncompleteEpsilonGreedyRefinementOrder<S, A>( rng, epsilon, root, subtrees );
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final RandomGenerator rng;
	private final double epsilon;
	
	public IncompleteEpsilonGreedyRefinementOrder( final RandomGenerator rng, final double epsilon,
									   final FsssAbstractStateNode<S, A> root,
									   final ArrayList<SubtreeRefinementOrder<S, A>> subtrees )
	{
		super( root, subtrees );
		this.rng = rng;
		this.epsilon = epsilon;
	}
	
	@Override
	public boolean isClosed()
	{
		return closed;
	}

	@Override
	protected SubtreeRefinementOrder<S, A> chooseSubtree()
	{
		final FsssAbstractActionNode<S, A> astar = root.astar();
		int astar_idx = Integer.MAX_VALUE; // This value ensures astar_idx > tree_idx if astar subtree is closed
		double max_U = -Double.MAX_VALUE;
		int max_idx = -1;
		for( int i = 0; i < subtrees.size(); ++i ) {
			final SubtreeRefinementOrder<S, A> t = subtrees.get( i );
			if( t.rootAction().a().equals( astar.a() ) ) {
				astar_idx = i;
				continue;
			}
			else {
				assert( !t.isClosed() );
				final double Ui = t.rootAction().U();
				if( Ui > max_U ) {
					max_U = Ui;
					max_idx = i;
				}
			}
		}
		final int tree_idx;
		if( rng.nextDouble() < epsilon ) {
			// Greedy choice
			tree_idx = max_idx;
		}
		else {
			// Uniform random choice
			final int candidate = rng.nextInt( subtrees.size() - 1 );
			// Adjust index to skip optimal subtree
			tree_idx = (candidate < astar_idx ? candidate : candidate + 1);
		}
		final SubtreeRefinementOrder<S, A> selected_subtree	= subtrees.get( tree_idx );
		return selected_subtree;
	}
}
