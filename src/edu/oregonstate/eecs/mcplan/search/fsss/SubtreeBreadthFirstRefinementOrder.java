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

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * Abstract base class for a SubtreeRefinementOrder that considers AANs in
 * a breadth-first order. Subclasses must override the chooseSplit() function.
 *
 * @param <S>
 * @param <A>
 */
public class SubtreeBreadthFirstRefinementOrder<S extends State, A extends VirtualConstructor<A>>
	extends SubtreeRefinementOrder<S, A>
{
	public static class Factory<S extends State, A extends VirtualConstructor<A>>
		implements SubtreeRefinementOrder.Factory<S, A>
	{
//		private final SplitChooser.Factory<S, A> split_chooser;
		private final boolean randomize;
	
		public Factory( //final SplitChooser.Factory<S, A> split_chooser,
						final boolean randomize )
		{
//			this.split_chooser = split_chooser;
			this.randomize = randomize;
		}

		@Override
		public SubtreeRefinementOrder<S, A> create( final FsssParameters parameters,
				final FsssModel<S, A> model, final FsssAbstractActionNode<S, A> root )
		{
			return new SubtreeBreadthFirstRefinementOrder<S, A>(
				parameters, model, root,
//				split_chooser.createSplitChooser( parameters, model ),
				randomize );
		}
		
		@Override
		public String toString()
		{
//			return "SubtreeBreadthFirst(randomize = " + randomize + "; " + split_chooser + ")";
			return "SubtreeBreadthFirst(randomize = " + randomize + ")";
		}
	}
	
	// -----------------------------------------------------------------------
	
	protected final FsssParameters parameters;
	protected final FsssModel<S, A> model;
	protected final FsssAbstractActionNode<S, A> root_action;
//	protected final SplitChooser<S, A> split_chooser;
	protected final boolean randomize;
	
	private final ArrayList<FsssAbstractActionNode<S, A>> current_layer
		= new ArrayList<FsssAbstractActionNode<S, A>>();
	private final ArrayList<FsssAbstractActionNode<S, A>> next_layer
		= new ArrayList<FsssAbstractActionNode<S, A>>();
	
	private boolean closed = false;
	
	public SubtreeBreadthFirstRefinementOrder( final FsssParameters parameters,
											   final FsssModel<S, A> model,
											   final FsssAbstractActionNode<S, A> root_action,
//											   final SplitChooser<S, A> split_chooser,
											   final boolean randomize )
	{
		this.parameters = parameters;
		this.model = model;
//		this.split_chooser = split_chooser;
		this.root_action = root_action;
		this.randomize = randomize;
		
		current_layer.add( root_action );
	}
	
	
	// -----------------------------------------------------------------------
	
	@Override
	public final FsssAbstractActionNode<S, A> rootAction()
	{
		return root_action;
	}
	
	@Override
	public final boolean isClosed()
	{
		return closed;
	}
	
	/**
	 * After a call to refineSubtree(), isClosed() = true if there were
	 * no more refinements to perform. In this case, refineSubtree() will
	 * not have performed a refinement.
	 * @return
	 */
	@Override
	public final void refine()
	{
		while( true ) {
			// If current layer is empty, swap layers
			if( current_layer.isEmpty() ) {
				// If next layer is also empty, this subtree is fully refined.
				if( next_layer.isEmpty() ) {
//					System.out.println( "\tSubtree: Fully refined" );
					closed = true;
					return;
				}
				
				current_layer.addAll( next_layer );
				next_layer.clear();
			}
			
			// Find a state node to refine
			final int i = (randomize ? model.rng().nextInt( current_layer.size() ) : 0);
//			final FsssAbstractActionNode<S, A> aan = current_layer.peekFirst();
			final FsssAbstractActionNode<S, A> aan = current_layer.get( i );
			
//			final SplitChoice<S, A> choice = split_chooser.chooseSplit( aan );
//			if( choice == null ) {
//				closeNode( i );
//				continue;
//			}
//			else if( choice.split == null ) {
//				// Node was homogeneous with respect to the base representation
//				choice.dn.close();
//				continue;
//			}
			
			// Refine aan
//			refine( aan, choice.dn, choice.split );
			final RefineableClassifierRepresenter<S, A> repr = (RefineableClassifierRepresenter<S, A>) aan.repr;
			final Object proposal = repr.proposeRefinement( aan );
			
			if( proposal == null ) {
				closeNode( i );
				continue;
			}
			
			repr.refine( aan, proposal );
			upSample( aan, parameters );
			backupToRoot( aan );
			
			break;
		}
	}
	
	// -----------------------------------------------------------------------
	
	// FIXME: Is it possible that a closed node could later become re-opened
	// due to a call to fsss()? If it happened, it would be because successors
	// are added to an AAN after closeNode() has already been called on it.
	//
	// I *think* we're OK because an un-Expanded node should never be refined.
	private void closeNode( final int i )
	{
//			System.out.println( "\tBuilder: closing " + aan );
		// All abstract state nodes are singletons -> remove this
		// state node and add all of its successors.
//		final FsssAbstractActionNode<S, A> check = current_layer.pollFirst();
		final FsssAbstractActionNode<S, A> aan = current_layer.remove( i );
//		System.out.println( "\tSubtree: Closing " + aan );
//		assert( aan == check );
		for( final FsssAbstractStateNode<S, A> asn : aan.successors() ) {
			if( !asn.isTerminal() ) {
				for( final FsssAbstractActionNode<S, A> aan_prime : asn.successors() ) {
//						System.out.println( "\t\tAdding" + aan_prime );
					next_layer.add( aan_prime );
				}
			}
		}
	}


	/**
	 * Note: We don't have to do anything, because new nodes are added when
	 * their parents are closed.
	 */
	@Override
	public void addNewStateNode( final FsssAbstractStateNode<S, A> asn )
	{ }


	@Override
	public boolean isActive()
	{
		// TODO Auto-generated method stub
		return false;
	}
}
