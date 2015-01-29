package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * Abstract base class for a SubtreeRefinementOrder that considers AANs in
 * a breadth-first order. Subclasses must override the chooseSplit() function.
 *
 * @param <S>
 * @param <A>
 */
public abstract class SubtreeBreadthFirstRefinementOrder<S extends State, A extends VirtualConstructor<A>>
	implements SubtreeRefinementOrder<S, A>
{
	protected final FsssParameters parameters;
	protected final FsssModel<S, A> model;
	protected final FsssAbstractActionNode<S, A> root_action;
	
	private final Deque<FsssAbstractActionNode<S, A>> current_layer
		= new ArrayDeque<FsssAbstractActionNode<S, A>>();
	private final Deque<FsssAbstractActionNode<S, A>> next_layer
		= new ArrayDeque<FsssAbstractActionNode<S, A>>();
	
	private boolean closed = false;
	
	public SubtreeBreadthFirstRefinementOrder( final FsssParameters parameters,
											   final FsssModel<S, A> model,
											   final FsssAbstractActionNode<S, A> root_action )
	{
		this.parameters = parameters;
		this.model = model;
		this.root_action = root_action;
		
		current_layer.addLast( root_action );
	}
	
	/**
	 * Chooses an attribute and value to split on. Must return 'null' if no
	 * more refinements should be attempted on 'aan'. Must return a non-null
	 * SplitChoice with non-null .dn and null .split member to indicate that no
	 * more refinements should be attempted for the .dn member.
	 * 
	 * @param aan
	 * @return
	 */
	protected abstract SplitChoice<S, A> chooseSplit( final FsssAbstractActionNode<S, A> aan );
	
	protected static class Split
	{
		public final int attribute;
		public final double value;
		
		public Split( final int attribute, final double value )
		{
			this.attribute = attribute;
			this.value = value;
		}
	}
	
	protected static class SplitChoice<S extends State, A extends VirtualConstructor<A>>
	{
		public final RefineablePartitionTreeRepresenter<S, A>.DataNode dn;
		public final Split split;
		
		public SplitChoice( final RefineablePartitionTreeRepresenter<S, A>.DataNode dn, final Split split )
		{
			this.dn = dn;
			this.split = split;
		}
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
					closed = true;
					return;
				}
				
				current_layer.addAll( next_layer );
				next_layer.clear();
			}
			
			// Find a state node to refine
			final FsssAbstractActionNode<S, A> aan = current_layer.peekFirst();
			final SplitChoice<S, A> choice = chooseSplit( aan );
			if( choice == null ) {
				closeNode( aan );
				continue;
			}
			else if( choice.split == null ) {
				// Node was homogeneous with respect to the base representation
				choice.dn.close();
				continue;
			}
			
			// Refine dn
			refine( aan, choice.dn, choice.split );
			upSample( aan );
			backupToRoot( aan );
			
			break;
		}
	}
	
	// -----------------------------------------------------------------------
	
	private void closeNode( final FsssAbstractActionNode<S, A> aan )
	{
//			System.out.println( "\tBuilder: closing " + aan );
		// All abstract state nodes are singletons -> remove this
		// state node and add all of its successors.
		final FsssAbstractActionNode<S, A> check = current_layer.pollFirst();
		assert( aan == check );
		for( final FsssAbstractStateNode<S, A> asn : aan.successors() ) {
			if( !asn.isTerminal() ) {
				for( final FsssAbstractActionNode<S, A> aan_prime : asn.successors() ) {
//						System.out.println( "\t\tAdding" + aan_prime );
					next_layer.addLast( aan_prime );
				}
			}
		}
	}
	
	/**
	 * Calls backup() along the path from 'aan' to the root node.
	 * @param aan
	 */
	private void backupToRoot( final FsssAbstractActionNode<S, A> aan )
	{
		final FsssAbstractStateNode<S, A> s = aan.predecessor;
		s.backup();
		if( s.predecessor != null ) {
			s.predecessor.backup();
			backupToRoot( s.predecessor );
		}
	}
	
	private void upSample( final FsssAbstractActionNode<S, A> aan )
	{
		// Don't sample if we're at the limit, but we still need to do
		// backups because buildSubtree2() does not do them.
		final Map<FsssAbstractStateNode<S, A>, ArrayList<FsssStateNode<S, A>>> added;
		if( model.sampleCount() < parameters.max_samples ) {
			added = aan.upSample( parameters.width );
		}
		else {
			added = new HashMap<FsssAbstractStateNode<S, A>, ArrayList<FsssStateNode<S, A>>>();
		}
		
		for( final FsssAbstractStateNode<S, A> sn : aan.successors() ) {
			// If the node has not been expanded yet, do not upSample
			if( sn.nvisits() == 0 ) {
//					System.out.println( "!!\t Not recursively upSampling " + sn );
//					System.out.println( "!!\t nsuccessors = " + sn.nsuccessors() );
				continue;
			}
			
			final ArrayList<FsssStateNode<S, A>> sn_added = added.get( sn );
			if( sn_added != null ) {
				sn.upSample( sn_added, parameters.width );
			}
			
			if( sn.isTerminal() ) {
				sn.leaf();
//					for( final FsssAbstractActionNode<S, A> aan_prime : sn.successors() ) {
//						aan_prime.leaf();
//					}
			}
			else {
				for( final FsssAbstractActionNode<S, A> aan_prime : sn.successors() ) {
					upSample( aan_prime );
				}
				sn.backup();
			}
			
		}
		aan.backup();
	}
	
	/**
	 * Refines 'dn', which must be a successor of 'aan'.
	 * @param aan
	 * @param dn
	 * @param split
	 * @return
	 */
	private void refine( final FsssAbstractActionNode<S, A> aan,
						 final RefineablePartitionTreeRepresenter<S, A>.DataNode dn,
						 final Split split )
	{
//		System.out.println( "\tBuilder: refining " + dn.aggregate + " below " + aan );
		
		final RefineablePartitionTreeRepresenter<S, A> repr = aan.repr;
		
//			final RefineablePartitionTreeRepresenter<S, A>.BinarySplitNode b
		final ArrayList<FsssAbstractStateNode<S, A>> parts
			= repr.refine( aan, dn, split.attribute, split.value );
		
//			final ArrayList<FsssAbstractStateNode<S, A>> parts = new ArrayList<FsssAbstractStateNode<S, A>>();
//			for( final RefineablePartitionTreeRepresenter<S, A>.DataNode d : Fn.in( b.children() ) ) {
//				parts.add( d.aggregate );
//			}
		
		aan.splitSuccessor( dn.aggregate, parts );
		dn.aggregate = null;
	}
}
