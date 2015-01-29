package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

public class FsssTreeStatistics<S extends State, A extends VirtualConstructor<A>>
{
	public static final class SubtreeStatistics<S extends State, A extends VirtualConstructor<A>>
	{
		/**
		 * Number of abstract state nodes below root action.
		 */
		public MeanVarianceAccumulator abstract_subtree_size = new MeanVarianceAccumulator();
		
		/**
		 * Number of ground state nodes below root action.
		 */
		public MeanVarianceAccumulator ground_subtree_size = new MeanVarianceAccumulator();
		
		/**
		 * Maximum depth of subtree.
		 */
		public MeanVarianceAccumulator max_depth = new MeanVarianceAccumulator();
		
		/**
		 * Mean depth.
		 */
		public MeanVarianceAccumulator mean_depth = new MeanVarianceAccumulator();
		
		/**
		 * Number of leaf state nodes.
		 */
		public MeanVarianceAccumulator num_leaves = new MeanVarianceAccumulator();
		
		/**
		 * Branching factor by depth.
		 */
		public final ArrayList<MeanVarianceAccumulator> depth_branching = new ArrayList<MeanVarianceAccumulator>();
	}
	
	/**
	 * Total number of samples generated.
	 */
	public MeanVarianceAccumulator num_samples = new MeanVarianceAccumulator();
	
	/**
	 * Statistics for optimal subtrees.
	 */
	public final SubtreeStatistics<S, A> optimal_subtree = new SubtreeStatistics<S, A>();
	
	/**
	 * Statistics for non-optimal subtrees.
	 */
	public final SubtreeStatistics<S, A> nonoptimal_subtrees = new SubtreeStatistics<S, A>();
	
	// All of these must be reset in visitRoot()
	private int root_depth = 0;
	private int subtree_max_depth = 0;
	private int subtree_abstract_size = 0;
	private int subtree_ground_size = 0;
	private int subtree_num_leaves = 0;
	private final int[] subtree_depth_branching;
	private int tree_num_samples = 0;
	
	private A subtree_action = null;
	private SubtreeStatistics<S, A> subtree_stats = null;
	
	public FsssTreeStatistics( final int depth )
	{
		subtree_depth_branching = new int[depth];
		for( int i = 0; i < depth; ++i ) {
			optimal_subtree.depth_branching.add( new MeanVarianceAccumulator() );
			nonoptimal_subtrees.depth_branching.add( new MeanVarianceAccumulator() );
		}
	}
	
	/**
	 * Turns the decreasing, 1-indexed depth of FSSS into an increasing,
	 * 0-indexed depth for use in our data structures.
	 * @param decreasing_depth
	 * @return
	 */
	private int increasingDepth( final int decreasing_depth )
	{
		return root_depth - decreasing_depth;
	}
	
	public void visitRoot( final FsssAbstractStateNode<S, A> root )
	{
		root_depth = root.depth;
		tree_num_samples = 0;
		
		final FsssAbstractActionNode<S, A> astar = root.astar();
		
		for( final FsssAbstractActionNode<S, A> aan : root.successors() ) {
			subtree_max_depth = 0;
			subtree_abstract_size = 0;
			subtree_ground_size = 0;
			subtree_num_leaves = 0;
			Fn.assign( subtree_depth_branching, 0 );
			
			subtree_action = aan.a();
			if( aan == astar ) {
//				System.out.println( "\t\tLogging optimal_subtree" );
				subtree_stats = optimal_subtree;
			}
			else {
//				System.out.println( "\t\tLogging nonoptimal_subtree" );
				subtree_stats = nonoptimal_subtrees;
			}
			
			visitActionNode( aan );
			
			subtree_stats.max_depth.add( subtree_max_depth );
			subtree_stats.abstract_subtree_size.add( subtree_abstract_size );
			subtree_stats.ground_subtree_size.add( subtree_ground_size );
			subtree_stats.num_leaves.add( subtree_num_leaves );
			for( int i = 0; i < subtree_depth_branching.length; ++i ) {
				subtree_stats.depth_branching.get( i ).add( subtree_depth_branching[i] );
			}
		}
		
		num_samples.add( tree_num_samples );
	}
	
	public void visitActionNode( final FsssAbstractActionNode<S, A> aan )
	{
		final int increasing_depth = increasingDepth( aan.depth );
		subtree_depth_branching[increasing_depth] += aan.nsuccessors();
//		subtree_stats.depth_branching.get( increasing_depth ).add( aan.nsuccessors() );
		
		for( final FsssAbstractStateNode<S, A> asn : aan.successors() ) {
			visitStateNode( asn );
		}
	}
	
	public void visitStateNode( final FsssAbstractStateNode<S, A> asn )
	{
		subtree_abstract_size += 1;
		subtree_ground_size += asn.states().size();
		tree_num_samples += asn.states().size();
		
		if( asn.isTerminal() || asn.nvisits() == 0 ) {
			final int increasing_depth = increasingDepth( asn.depth );
			subtree_stats.mean_depth.add( increasing_depth );
			subtree_max_depth = Math.max( subtree_max_depth, increasing_depth );
			subtree_num_leaves += 1;
		}
		
		for( final FsssAbstractActionNode<S, A> aan : asn.successors() ) {
			visitActionNode( aan );
		}
	}
}
