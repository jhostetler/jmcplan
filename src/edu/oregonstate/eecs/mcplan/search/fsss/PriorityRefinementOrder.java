/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * A second-generation refinement order implementation, intended to subsume
 * all of the existing RefinementOrder / SubtreeRefinementOrder types. This
 * implementation follows the pseudocode in the UAI paper more closely.
 * 
 * At any given time, the ASNs in the search tree are in one of three states:
 *     1. Active - Expanded and not pure
 *     2. Inactive - Unexpanded or pure, but might become Active again
 *     3. Closed - Inactive and will never be Active again
 * 
 * This class maintains a priority ordering over the Active ASNs, and manages
 * the bookkeeping for deciding which state each ASN is in. Priority is
 * calculated by a user-supplied function. ASNs with equal priority are kept
 * in a collection together. When it is time to choose an ASN to refine, one
 * is chosen at random from the lowest-priority collection.
 * 
 * The existing breadth-first ordering can be implemented by setting priority
 * equal to depth. The fully-randomized ordering assigns equal priority to
 * every ASN.
 */
public abstract class PriorityRefinementOrder<S extends State, A extends VirtualConstructor<A>>
	implements RefinementOrder<S, A>
{
	public static interface Factory<S extends State, A extends VirtualConstructor<A>>
	{
		public PriorityRefinementOrder<S, A> create(
			final FsssParameters parameters, final FsssModel<S, A> model,
			final FsssAbstractStateNode<S, A> root );
	}
	
	// -----------------------------------------------------------------------
	
	private final ch.qos.logback.classic.Logger Log = LoggerManager.getLogger( "log.search" );
	
	private final Map<FsssAbstractStateNode<S, A>, DataNode<S, A>> dn_map
		= new HashMap<FsssAbstractStateNode<S, A>, DataNode<S, A>>();
	
	private final Set<FsssAbstractStateNode<S, A>> active_set
		= new HashSet<FsssAbstractStateNode<S, A>>();
	
	private final Set<FsssAbstractStateNode<S, A>> inactive_set
		= new HashSet<FsssAbstractStateNode<S, A>>();
	
	/** Maintains priority ordering of node sets. */
	private final TreeMap<Double, PrioritySet> priority
		= new TreeMap<Double, PrioritySet>();
	
	private class PrioritySet
	{
		public final double priority;
		public final ArrayList<FsssAbstractStateNode<S, A>> nodes;
		
		public PrioritySet( final double priority )
		{
			this( priority, new ArrayList<FsssAbstractStateNode<S, A>>() );
		}
		
		public PrioritySet( final double priority, final ArrayList<FsssAbstractStateNode<S, A>> nodes )
		{
			this.priority = priority;
			this.nodes = nodes;
		}
		
		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder();
			sb.append( "[" );
			for( int i = 0; i < nodes.size(); ++i ) {
				if( i > 0 ) {
					sb.append( "," );
				}
				final FsssAbstractStateNode<S, A> asn = nodes.get( i );
				sb.append( "@" + Integer.toHexString( System.identityHashCode( asn ) ) );
			}
			sb.append( "]" );
			return sb.toString();
		}
	}
	
	/** Allows retrieval of priority elements from ASN keys. */
	private final Map<FsssAbstractStateNode<S, A>, PrioritySet> priority_index
		= new HashMap<FsssAbstractStateNode<S, A>, PrioritySet>();
	
	private final FsssParameters parameters;
	private final FsssModel<S, A> model;
	private final FsssAbstractStateNode<S, A> root;
	
	public PriorityRefinementOrder( final FsssParameters parameters, final FsssModel<S, A> model,
									final FsssAbstractStateNode<S, A> root )
	{
		this.parameters = parameters;
		this.model = model;
		this.root = root;
		
		for( final FsssAbstractActionNode<S, A> aan : root.successors() ) {
			for( final FsssAbstractStateNode<S, A> asn_prime : aan.successors() ) {
				addSubtree( asn_prime, aan.repr.getDataNode( asn_prime ) );
			}
		}
	}
	
	@Override
	public boolean isClosed()
	{
		return active_set.isEmpty(); // && inactive_set.isEmpty();
	}
	
	protected abstract double calculatePriority( final FsssAbstractStateNode<S, A> asn );
	
	/**
	 * Calls backup() along the path from 'aan' to the root node.
	 * @param aan
	 */
	private void backupToRoot( final FsssAbstractActionNode<S, A> aan )
	{
		aan.backup();
		final FsssAbstractStateNode<S, A> s = aan.predecessor;
		s.backup();
		if( s.predecessor != null ) {
//			s.predecessor.backup();
			backupToRoot( s.predecessor );
		}
	}
	
	/**
	 * Recursively adds samples to each action node descendant of 'asn' to
	 * re-establish the sparse sampling invariant.
	 * @param asn
	 */
	private void upSample( final FsssAbstractStateNode<S, A> asn )
	{
		Log.debug( "\tupSample(): " + asn );
		
		if( !asn.isExpanded() ) {
			return;
		}
		
		for( final FsssAbstractActionNode<S, A> aan : asn.successors() ) {
			// 'added' will contain the ground states added to each ASN successor
			final Map<FsssAbstractStateNode<S, A>, ArrayList<FsssStateNode<S, A>>> added
				= aan.upSample( parameters.width, parameters.budget );
			
			for( final FsssAbstractStateNode<S, A> asn_succ : aan.successors() ) {
				// Un-expanded nodes have no AAN children to sample
				if( !asn_succ.isExpanded() ) {
					continue;
				}
				
				final ArrayList<FsssStateNode<S, A>> sn_added = added.get( asn_succ );
				if( sn_added != null ) {
					asn_succ.addActionNodes( sn_added );
				}
				
//				if( asn_succ.isTerminal() ) {
////					asn_succ.leaf();
//				}
//				else {
//					upSample( asn_succ );
////					asn_succ.backup();
//				}
				
				upSample( asn_succ );
			}
			
			assert( aan.nsuccessors() > 0 );
			aan.backup();
		}
		
		if( asn.isTerminal() ) {
			asn.leaf();
		}
		else {
			asn.backup();
		}
	}
	
	/**
	 * Removes asn and its associated objects from all data structures.
	 * @param asn
	 */
	private void forgetStateNode( final FsssAbstractStateNode<S, A> asn )
	{
		Log.debug( "\tForgetting: " + asn );
		active_set.remove( asn );
		inactive_set.remove( asn );
		final PrioritySet pset = priority_index.get( asn );
		if( pset != null ) {
			pset.nodes.remove( asn );
			if( pset.nodes.isEmpty() ) {
				priority.remove( pset.priority );
			}
			final DataNode<S, A> check = dn_map.remove( asn );
			assert( check != null );
		}
		else {
			assert( !asn.isActive() );
			assert( !dn_map.containsKey( asn ) );
		}
	}
	
	/**
	 * Calls forgetStateNode() recursively on the subtree rooted at asn.
	 * @param asn
	 */
	private void forgetSubtree( final FsssAbstractStateNode<S, A> asn )
	{
		forgetStateNode( asn );
		for( final FsssAbstractActionNode<S, A> aan : asn.successors() ) {
			for( final FsssAbstractStateNode<S, A> asn_succ : aan.successors() ) {
				forgetSubtree( asn_succ );
			}
		}
	}
	
	/**
	 * Adds asn and dn to all data structures.
	 * @param asn
	 * @param dn
	 */
	private void addStateNode( final FsssAbstractStateNode<S, A> asn, final DataNode<S, A> dn )
	{
		// Activate the new subtrees
		Log.debug( "\t\tAdding " + asn );
		assert( dn.aggregate == asn );
		assert( !active_set.contains( asn ) );
		assert( !inactive_set.contains( asn ) );
		if( asn.isTerminal() ) {
			Log.debug( "\t\t\tTerminal" );
			return;
		}
		else if( !asn.isExpanded() ) {
			Log.debug( "\t\t\tNot expanded" );
			return;
		}
		else if( asn.isPure() ) {
			Log.debug( "\t\t\tPure" );
			// We can close the node if all of its ancestors are closed.
			// It is sufficient to check if its parent is closed, because:
			//		1. The first time the root node is not refined, its
			//		   parent is trivially closed, thus all of its
			//		   predecessors are closed and the root becomes closed.
			//		2. Inductively, if the first time a node at depth d
			//		   is not refined its parent is closed, then all of its
			//		   predecessors are closed.
			final FsssAbstractStateNode<S, A> asn_pred = asn.predecessor.predecessor;
			if( active_set.contains( asn_pred ) || inactive_set.contains( asn_pred ) ) {
				Log.debug( "\t\t\tInactive" );
				inactive_set.add( asn );
				return;
			}
			else {
				Log.debug( "\t\t\tClosed" );
				return;
			}
		}
		else {
			Log.debug( "\t\t\tNot pure" );
			active_set.add( asn );
		}
		
		final double p = calculatePriority( asn );
		PrioritySet pset = priority.get( p );
		if( pset == null ) {
			pset = new PrioritySet( p );
			priority.put( p, pset );
		}
		pset.nodes.add( asn );
		priority_index.put( asn, pset );
		
		dn_map.put( asn, dn );
	}
	
	/**
	 * Calls addStateNode() recursively on the subtree rooted at asn.
	 * @param asn
	 * @param dn
	 */
	private void addSubtree( final FsssAbstractStateNode<S, A> asn, final DataNode<S, A> dn )
	{
		addStateNode( asn, dn );
		for( final FsssAbstractActionNode<S, A> aan : asn.successors() ) {
			Log.debug( "\taddSubtree(): aan " + aan );
			aan.repr.prune();
			for( final DataNode<S, A> dn_succ : aan.repr.dt_leaves ) {
				Log.debug( "\t\tdn_succ " + dn_succ + " / " + dn_succ.aggregate );
				FsssTest.printDecisionTree( dn_succ, Log, 1 );
				addSubtree( dn_succ.aggregate, dn_succ );
			}
		}
	}

	@Override
	public void refine()
	{
		// Questions:
		// 1. Should active_set implicitly be everything that's in priority?
		
		// Find random ASN from lowest priority set
		Log.debug( "priority: " + priority );
		final PrioritySet pset = priority.firstEntry().getValue();
		final int r = model.rng().nextInt( pset.nodes.size() );
		final FsssAbstractStateNode<S, A> to_refine = pset.nodes.get( r );
		final FsssAbstractActionNode<S, A> aan_parent = to_refine.predecessor;
		final DataNode<S, A> dn = dn_map.get( to_refine );
		
		Log.debug( "\tRefining: " + to_refine );
		Log.debug( "\t\tBelow: " + aan_parent );
		Log.debug( "\t\tDN: " + dn );
		
		// Do refinement
		assert( dn.split == null );
		assert( dn.aggregate.isExpanded() );
		assert( !dn.aggregate.isPure() || inactive_set.contains( dn.aggregate ) );
		forgetSubtree( dn.aggregate );
		((RefineableClassifierRepresenter<S, A>) to_refine.predecessor.repr).refine( dn );
		
		// Complete PAR operation
		for( final DataNode<S, A> child : Fn.in( dn.split.children() ) ) {
			upSample( child.aggregate );
		}
		backupToRoot( aan_parent );
		
		Log.debug( "..... Before AFSSS ....." );
		FsssTest.printTree( root, Log, 2 );
		Log.debug( "........................" );
		
		// Activate the new subtrees before FSSS. Set of nodes in new subtree
		// at this point will be disjoint from set of nodes expanded by FSSS.
		for( final DataNode<S, A> child : Fn.in( dn.split.children() ) ) {
			addSubtree( child.aggregate, child );
		}
		
		// AB-FSSS
		final AbstractFsss<S, A> fsss = new AbstractFsss<S, A>( parameters, model, root );
		fsss.setLoggingEnabled( true );
		final ExpandedNodeCollector<S, A> collector = new ExpandedNodeCollector<S, A>();
		fsss.addListener( collector );
		fsss.run();
		
		// Add newly-expanded nodes
		for( final ArrayList<FsssAbstractStateNode<S, A>> nodes : collector.expanded.values() ) {
			for( final FsssAbstractStateNode<S, A> expanded : nodes ) {
				Log.debug( "\tExpanded: " + expanded );
				final DataNode<S, A> expanded_dn = expanded.predecessor.repr.getDataNode( expanded );
				assert( expanded_dn != null );
				addStateNode( expanded, expanded_dn );
			}
		}
	}

}
