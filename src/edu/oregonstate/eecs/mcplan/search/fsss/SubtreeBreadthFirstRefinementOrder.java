package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;
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
public class SubtreeBreadthFirstRefinementOrder<S extends State, A extends VirtualConstructor<A>>
	implements SubtreeRefinementOrder<S, A>
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
			final boolean refined = repr.refine( aan );
			if( !refined ) {
				closeNode( i );
				continue;
			}
			upSample( aan );
			backupToRoot( aan );
			
			break;
		}
	}
	
	// -----------------------------------------------------------------------
	
	// FIXME: Is it possible that a closed node could later become re-opened
	// due to a call to fsss()? If it happened, it would be because successors
	// are added to an AAN has new successors added after closeNode() has
	// already been called on it.
	//
	// I *think* we're OK because an un-Expanded node should never be refined.
	private void closeNode( final int i )
	{
//			System.out.println( "\tBuilder: closing " + aan );
		// All abstract state nodes are singletons -> remove this
		// state node and add all of its successors.
//		final FsssAbstractActionNode<S, A> check = current_layer.pollFirst();
		final FsssAbstractActionNode<S, A> aan = current_layer.remove( i );
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
//		final Map<FsssAbstractStateNode<S, A>, ArrayList<FsssStateNode<S, A>>> added;
//		if( model.sampleCount() < parameters.max_samples ) {
//			added = aan.upSample( parameters.width, parameters.max_samples );
//		}
//		else {
//			added = new HashMap<FsssAbstractStateNode<S, A>, ArrayList<FsssStateNode<S, A>>>();
//		}
		
		final Map<FsssAbstractStateNode<S, A>, ArrayList<FsssStateNode<S, A>>> added
//			= aan.upSample( parameters.width, parameters.max_samples );
			= aan.upSample( parameters.width, parameters.budget );
		
		for( final FsssAbstractStateNode<S, A> sn : aan.successors() ) {
			// If the node has not been expanded yet, do not upSample
			if( sn.nvisits() == 0 ) {
//					System.out.println( "!!\t Not recursively upSampling " + sn );
//					System.out.println( "!!\t nsuccessors = " + sn.nsuccessors() );
				continue;
			}
			
			final ArrayList<FsssStateNode<S, A>> sn_added = added.get( sn );
			if( sn_added != null ) {
				sn.addActionNodes( sn_added );
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
		if( aan.nsuccessors() > 0 ) {
			aan.backup();
		}
//		else {
//			FsssTest.printTree( FsssTest.findRoot( aan ), System.out, 1 );
//			System.out.println( "! " + aan );
//			System.exit( 0 );
//		}
	}
	
//		/**
//	 * Refines 'dn', which must be a successor of 'aan'.
//	 * @param aan
//	 * @param dn
//	 * @param split
//	 * @return
//	 */
//	private void refine( final FsssAbstractActionNode<S, A> aan )
//	{
////		System.out.println( "\tBuilder: refining " + dn.aggregate + " below " + aan );
//		final ArrayList<FsssAbstractStateNode<S, A>> parts = aan.repr.refine( aan );
//		aan.splitSuccessor( dn.aggregate, parts );
//		dn.aggregate = null;
//	}
	
//	/**
//	 * Refines 'dn', which must be a successor of 'aan'.
//	 * @param aan
//	 * @param dn
//	 * @param split
//	 * @return
//	 */
//	private void refine( final FsssAbstractActionNode<S, A> aan,
//						 final RefineablePartitionTreeRepresenter<S, A>.DataNode dn,
//						 final Split split )
//	{
////		System.out.println( "\tBuilder: refining " + dn.aggregate + " below " + aan );
//
//		final RefineablePartitionTreeRepresenter<S, A> repr = aan.repr;
//
////			final RefineablePartitionTreeRepresenter<S, A>.BinarySplitNode b
//		final ArrayList<FsssAbstractStateNode<S, A>> parts
//			= repr.refine( aan, dn, split.attribute, split.value );
//
////			final ArrayList<FsssAbstractStateNode<S, A>> parts = new ArrayList<FsssAbstractStateNode<S, A>>();
////			for( final RefineablePartitionTreeRepresenter<S, A>.DataNode d : Fn.in( b.children() ) ) {
////				parts.add( d.aggregate );
////			}
//
//		aan.splitSuccessor( dn.aggregate, parts );
//		dn.aggregate = null;
//	}
}
