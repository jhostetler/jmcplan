/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.abstraction.IndexRepresentation;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class RefineableRandomPartitionRepresenter<S extends State, A extends VirtualConstructor<A>>
	extends RefineablePartitionTreeRepresenter<S, A>
{
	public static class Abstraction<S extends State, A extends VirtualConstructor<A>>
		extends FsssAbstraction<S, A>
	{
		private final FsssModel<S, A> model;
	
		public Abstraction( final FsssModel<S, A> model )
		{
			this.model = model;
		}
		
		@Override
		public String toString()
		{
			return "RandomPartitionRefinement";
		}
		
		@Override
		public ClassifierRepresenter<S, A> createRepresenter()
		{
			return new RefineableRandomPartitionRepresenter<S, A>( model, this );
		}
	}
	
	// -----------------------------------------------------------------------

	public RefineableRandomPartitionRepresenter( final FsssModel<S, A> model,
			final FsssAbstraction<S, A> abstraction )
	{
		super( model, abstraction, null /*split_chooser*/ );
	}
	
	@Override
	public RefineableRandomPartitionRepresenter<S, A> create()
	{
		return new RefineableRandomPartitionRepresenter<S, A>( model, abstraction );
	}
	
	private int bestPath( final FactoredRepresentation<S> x, final DataNode<S, A> dn,
						  final ArrayList<DataNode<S, A>> path,
						  final ArrayList<DataNode<S, A>> best_path, final int min_count )
	{
		int ret = min_count;
		path.add( dn );
		if( dn.split == null ) {
			// Leaf node
			
//			System.out.println( "\tbestPath(): Leaf " + dn.aggregate );
//			assert( dn.aggregate != null );
			final int n = (dn.aggregate != null ? dn.aggregate.n() : 0);
			if( n < min_count ) {
				ret = n;
				best_path.clear();
				best_path.addAll( path );
			}
		}
		else {
			final MapBinarySplitNode<S, A> map_split = (MapBinarySplitNode<S, A>) dn.split;
			if( map_split.assignments.containsKey( x ) ) {
//				System.out.println( "\tFollowing " + map_split.child( x ) );
				ret = bestPath( x, map_split.child( x ), path, best_path, ret );
			}
			else {
				for( final DataNode<S, A> child : Fn.in( dn.split.children() ) ) {
					ret = bestPath( x, child, path, best_path, ret );
				}
			}
		}
		// Undo add
		path.remove( path.size() - 1 );
		return ret;
	}
	
	@Override
	protected DataNode<S, A> novelInstance( final DataNode<S, A> dt_root, final FactoredRepresentation<S> x )
	{
		// Find the path to the DT leaf with the fewest instances.
		final ArrayList<DataNode<S, A>> path = new ArrayList<DataNode<S, A>>();
		final ArrayList<DataNode<S, A>> best_path = new ArrayList<DataNode<S, A>>();
		final int n = bestPath( x, dt_root, path, best_path, Integer.MAX_VALUE );
//		for( final DataNode<S, A> dn : best_path ) {
//			System.out.println( "\t\t" + dn );
//		}
		
		for( int i = 0; i < best_path.size() - 1; ++i ) {
			final DataNode<S, A> parent = best_path.get( i );
			final DataNode<S, A> child = best_path.get( i + 1 );
			final DataNode<S, A> check = ((MapBinarySplitNode<S, A>) parent.split).assignments.put( x, child );
			// TODO: Debugging code
			if( check != null && check != child ) {
				System.out.println( "\t! check = " + check );
				System.out.println( "\t! child = " + child );
			}
			
			assert( check == null || check == child );
		}
		
		return best_path.get( best_path.size() - 1 );
	}
	
	@Override
	public void prune()
	{
		for( final DataNode<S, A> root : dt_roots.values() ) {
			if( root.split != null ) {
				pruneDtSubtree( root );
			}
			if( root.split == null ) {
				if( root.aggregate == null ) {
					System.out.println( "\t! prune(): null root " + root );
				}
				assert( root.aggregate != null );
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
		
		final ArrayList<DataNode<S, A>> leaves = new ArrayList<DataNode<S, A>>();
		for( final DataNode<S, A> succ : Fn.in( dn.split.children() ) ) {
			// First do the recursive call
			if( succ.split != null ) {
				assert( succ.aggregate == null );
				pruneDtSubtree( succ );
			}
			
			// succ.aggregate == null could have already been true, or it
			// could have become true during the recursive call
//			if( succ.split == null ) {
				if( succ.split == null && succ.aggregate == null ) {
					System.out.println( "\tpruntDtSubtree(): succ.aggregate == null " + succ );
					// The data node has no members;
					final boolean check = dt_leaves.remove( succ );
	//				assert( check );
				}
				else {
					leaves.add( succ );
				}
//			}
		}
		
//		final ArrayList<DataNode<S, A>> leaves = new ArrayList<DataNode<S, A>>();
//		for( final DataNode<S, A> succ : Fn.in( dn.split.children() ) ) {
//			if( succ.split != null ) {
//				assert( succ.aggregate == null );
//				pruneDtSubtree( succ );
//			}
//			else if( succ.aggregate == null ) {
//				// The data node has no members;
//				final boolean check = dt_leaves.remove( succ );
//				assert( check );
//			}
//			else {
//				leaves.add( succ );
//			}
//		}
		
		if( leaves.isEmpty() ) {
			System.out.println( "\tpruneDtSubtree(): no children for " + dn );
			// Branch is dead. This node will be removed when control
			// returns to parent.
			dn.aggregate = null;
			dn.split = null;
		}
		else if( leaves.size() == 1 ) {
			System.out.println( "\tpruneDtSubtree(): singleton child for " + dn );
			System.out.println( "\t\t" + leaves.get( 0 ) );
			// dn is a redundant split node because it has only one child
			dn.aggregate = leaves.get( 0 ).aggregate;
			dn.split = leaves.get( 0 ).split;
			assert( dn.aggregate != null ^ dn.split != null );
		}
		else {
			// Everything's fine
		}
	}
	
	@Override
	public Object proposeRefinement( final FsssAbstractActionNode<S, A> aan )
	{
		final DataNode<S, A> choice;
		while( true ) {
			final DataNode<S, A> candidate = largestChild( aan );
			if( candidate == null ) {
				// No more refinements to do on 'aan'
				return null;
			}
//			else if( candidate.aggregate.states().size() == 1 ) {
//				// Candidate is fully refined
////				System.out.println( "\tRefine: closing (singleton): " + candidate );
//				candidate.close();
//			}
			else {
				final ArrayList<FsssStateNode<S, A>> states = candidate.aggregate.states();
				final FactoredRepresentation<S> ex = states.get( 0 ).x();
				boolean pure = true;
				for( int i = 1; i < states.size(); ++i ) {
					if( ! states.get( i ).x().equals( ex ) ) {
						pure = false;
						break;
					}
				}
				
				if( pure ) {
					// Candidate is fully refined
//					System.out.println( "\tRefine: closing (pure): " + candidate );
//					candidate.close();
				}
				else {
					choice = candidate;
					break;
				}
			}
		}
		return choice;
	}
	
	@Override
	protected DataNode<S, A> createSplitNode( final FsssAbstractActionNode<S, A> aan, final Object proposal )
	{
		@SuppressWarnings( "unchecked" )
		final DataNode<S, A> choice = (DataNode<S, A>) proposal;
		
		assert( choice.split == null );
		choice.split = new MapBinarySplitNode<S, A>( dn_factory );
		
		for( final DataNode<S, A> dn_child : Fn.in( choice.split.children() ) ) {
			dn_child.aggregate = new FsssAbstractStateNode<S, A>(
				aan, model, abstraction, new IndexRepresentation<S>( dn_child.id ) );
		}
		
		final ArrayList<FsssStateNode<S, A>> shuffled = new ArrayList<FsssStateNode<S, A>>( choice.aggregate.states() );
		Fn.shuffle( model.rng(), shuffled );
		for( final FsssStateNode<S, A> gsn : shuffled ) {
			choice.split.addGroundStateNode( gsn );
		}
		
		return choice;
	}
	
	protected void createSplitNode( final DataNode<S, A> dn )
	{
		assert( dn.aggregate != null );
		assert( dn.split == null );
		dn.split = new MapBinarySplitNode<S, A>( dn_factory );
		
		for( final DataNode<S, A> dn_child : Fn.in( dn.split.children() ) ) {
			dn_child.aggregate = new FsssAbstractStateNode<S, A>(
				dn.aggregate.predecessor, model, abstraction, new IndexRepresentation<S>( dn_child.id ) );
		}
		
		final ArrayList<FsssStateNode<S, A>> shuffled = new ArrayList<FsssStateNode<S, A>>( dn.aggregate.states() );
		Fn.shuffle( model.rng(), shuffled );
		for( final FsssStateNode<S, A> gsn : shuffled ) {
			dn.split.addGroundStateNode( gsn );
		}
	}
	
	@Override
	public void refine( final DataNode<S, A> dn )
	{
		createSplitNode( dn );
		doSplit( dn );
	}
}
