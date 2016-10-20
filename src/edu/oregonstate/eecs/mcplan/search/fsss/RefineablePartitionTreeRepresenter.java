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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.abstraction.IndexRepresentation;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class RefineablePartitionTreeRepresenter<S extends State, A extends VirtualConstructor<A>>
	extends RefineableClassifierRepresenter<S, A>
{
	private final ch.qos.logback.classic.Logger Log = LoggerManager.getLogger( "log.search" );
	
	private final SplitChooser<S, A> split_chooser;
	
	public RefineablePartitionTreeRepresenter( final FsssModel<S, A> model, final FsssAbstraction<S, A> abstraction,
											   final SplitChooser<S, A> split_chooser )
	{
		super( model, abstraction );
		this.split_chooser = split_chooser;
	}
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.search.fsss.RefineableRepresenter#create()
	 */
	@Override
	public RefineablePartitionTreeRepresenter<S, A> create()
	{
		return new RefineablePartitionTreeRepresenter<S, A>( model, abstraction, split_chooser );
	}
	
	protected DataNode<S, A> createSplitNode( final FsssAbstractActionNode<S, A> aan, final Object proposal )
	{
		@SuppressWarnings( "unchecked" )
		final SplitChoice<S, A> choice = (SplitChoice<S, A>) proposal;
		createSplitNode( choice.dn, choice.split );
		return choice.dn;
//		assert( choice.dn.split == null );
//		choice.dn.split = new BinarySplitNode<S, A>( dn_factory, choice.split.attribute, choice.split.value );
//
//		for( final DataNode<S, A> dn_child : Fn.in( choice.dn.split.children() ) ) {
//			dn_child.aggregate = new FsssAbstractStateNode<S, A>(
//				aan, model, abstraction, new IndexRepresentation<S>( dn_child.id ) );
//		}
//
//		for( final FsssStateNode<S, A> gsn : choice.dn.aggregate.states() ) {
//			choice.dn.split.addGroundStateNode( gsn );
//		}
//
//		return choice.dn;
	}
	
	protected void createSplitNode( final DataNode<S, A> dn, final Split split )
	{
		assert( dn.aggregate != null );
		assert( dn.split == null );
		dn.split = new BinarySplitNode<S, A>( dn_factory, split.attribute, split.value );
		
		for( final DataNode<S, A> dn_child : Fn.in( dn.split.children() ) ) {
			dn_child.aggregate = new FsssAbstractStateNode<S, A>(
				dn.aggregate.predecessor, model, abstraction, new IndexRepresentation<S>( dn_child.id ) );
		}
		
		for( final FsssStateNode<S, A> gsn : dn.aggregate.states() ) {
			dn.split.addGroundStateNode( gsn );
		}
		
//		return dn;
	}
	
	@Override
	public Object proposeRefinement( final FsssAbstractActionNode<S, A> aan )
	{
		return split_chooser.chooseSplit( aan );
	}
	
	protected void doSplit( final DataNode<S, A> dn )
	{
		assert( dn.aggregate != null );
		assert( dn.split != null );
		final boolean check = dt_leaves.remove( dn );
		assert( check );
		Log.debug( "\tRefining {}", dn.aggregate );
		
		for( final DataNode<S, A> dn_child : Fn.in( dn.split.children() ) ) {
			dt_leaves.add( dn_child );
			dn_child.aggregate.visit();
		}
		
		final ArrayList<FsssAbstractStateNode<S, A>> parts = new ArrayList<FsssAbstractStateNode<S, A>>();
		for( final DataNode<S, A> dn_child : Fn.in( dn.split.children() ) ) {
			parts.add( dn_child.aggregate );
		}
		
		dn.aggregate.predecessor.splitSuccessor( dn.aggregate, parts );
		Log.debug( "\tdoSplit(): Setting aggregate to null: {}", dn );
		dn.aggregate = null; // Allow GC of the old ASN
	}
	
	@Override
	public void refine( final FsssAbstractActionNode<S, A> aan, final Object proposal )
	{
		assert( proposal != null );
		final DataNode<S, A> dn = createSplitNode( aan, proposal );
		doSplit( dn );
	}

	@Override
	public void refine( final DataNode<S, A> dn )
	{
		final Split split = split_chooser.chooseSplit( dn );
		createSplitNode( dn, split );
		doSplit( dn );
	}
	
	@Override
	public void prune()
	{
		// Note: regarding dt_leaves, it's easier to just clear it and
		// re-build it than to figure out which particular nodes should
		// be removed/added during pruning, so that's what we do.
		
		dt_leaves.clear();
		final Iterator<DataNode<S, A>> itr = dt_roots.values().iterator();
//		for( final DataNode<S, A> root : dt_roots.values() ) {
		while( itr.hasNext() ) {
			final DataNode<S, A> root = itr.next();
			if( root.split != null ) {
				pruneDtSubtree( root );
			}
			if( root.split == null ) {
				if( root.aggregate == null ) {
					// This happens when a refinement removes an entire
					// "legal action equivalence class" from the ASN. It was
					// previously thought to be an error because I was testing
					// on Saving, where this never happens.
					
					Log.warn( "Pruning entire DT: {}", root );
					itr.remove();
					continue;
				}
				
//				if( Log.isErrorEnabled() && root.aggregate == null ) {
//					Log.error( "\t! prune(): null root {}", root );
//
//					throw new RuntimeException();
//				}
				
//				assert( root.aggregate != null );
			}
			
			populateDtLeaves( root );
		}
	}
	
	private void populateDtLeaves( final DataNode<S, A> root )
	{
		final Deque<DataNode<S, A>> q = new ArrayDeque<DataNode<S, A>>();
		q.push( root );
		while( !q.isEmpty() ) {
			final DataNode<S, A> dn = q.pop();
			if( dn.aggregate != null ) {
				assert( dn.split == null );
				dt_leaves.add( dn );
			}
			else {
				for( final DataNode<S, A> succ : Fn.in( dn.split.children() ) ) {
					q.add( succ );
				}
			}
		}
	}
	
	/**
	 * If all of a split node's children have null aggregates, we want to
	 * remove the entire subtree under the split node. If all but one of its
	 * children have null aggregates, we want to promote the single non-null
	 * node to the level of the split node.
	 * @param dn
	 */
	private void pruneDtSubtree( final DataNode<S, A> dn )
	{
		assert( dn.split != null ); // dn is a split node
		assert( dn.aggregate == null );
		
		final ArrayList<DataNode<S, A>> children = new ArrayList<DataNode<S, A>>();
		for( final DataNode<S, A> succ : Fn.in( dn.split.children() ) ) {
			// First do the recursive call
			if( succ.split != null ) {
				assert( succ.aggregate == null );
				pruneDtSubtree( succ );
			}
			
			// succ.aggregate == null could have already been true, or it
			// could have become true during the recursive call
			if( succ.split == null && succ.aggregate == null ) {
				Log.debug( "\tpruntDtSubtree(): succ.aggregate == null {}", succ );
				// The data node has no members;
//				final boolean check = dt_leaves.remove( succ );
//				assert( check );
			}
			else {
				children.add( succ );
			}
		}
		
		if( children.isEmpty() ) {
			Log.debug( "\tpruneDtSubtree(): no children for {}", dn );
			// Branch is dead. This node will be removed when control
			// returns to parent.
			dn.aggregate = null;
			dn.split = null;
		}
		else if( children.size() == 1 ) {
			Log.debug( "\tpruneDtSubtree(): singleton child for {}", dn );
			Log.debug( "\t\t{}", children.get( 0 ) );
			// dn is a redundant split node because it has only one child
			// We promote it to the next highest level and update ancestor pointers
			dn.aggregate = children.get( 0 ).aggregate;
			dn.split = children.get( 0 ).split;
			// Exactly one of 'aggregate' and 'split' should be non-null
			assert( dn.aggregate != null ^ dn.split != null );
		}
		else {
			// Everything's fine
		}
	}
}
