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
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;
import edu.oregonstate.eecs.mcplan.util.MinMaxAccumulator;

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
	 * Total number of samples generated.
	 */
	public MinMaxAccumulator min_max_samples = new MinMaxAccumulator();
	
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
	private ArrayList<MeanVarianceAccumulator> subtree_depth_branching = null;
	public int tree_abstract_size = 0;
	public int tree_ground_size = 0;
	
	private A subtree_action = null;
	private SubtreeStatistics<S, A> subtree_stats = null;
	
	public FsssTreeStatistics( final int depth )
	{
//		subtree_depth_branching = new int[depth];
//		for( int i = 0; i < depth; ++i ) {
//			optimal_subtree.depth_branching.add( new MeanVarianceAccumulator() );
//			nonoptimal_subtrees.depth_branching.add( new MeanVarianceAccumulator() );
//		}
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
		tree_abstract_size = 0;
		tree_ground_size = 0;
		
		final FsssAbstractActionNode<S, A> astar = root.astar();
		
		for( final FsssAbstractActionNode<S, A> aan : root.successors() ) {
			subtree_max_depth = 0;
			subtree_abstract_size = 0;
			subtree_ground_size = 0;
			subtree_num_leaves = 0;
//			Fn.assign( subtree_depth_branching, 0 );
			subtree_depth_branching = new ArrayList<MeanVarianceAccumulator>();
			
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
			for( int i = 0; i < subtree_depth_branching.size(); ++i ) {
				if( subtree_stats.depth_branching.size() <= i ) {
					subtree_stats.depth_branching.add( new MeanVarianceAccumulator() );
				}
				subtree_stats.depth_branching.get( i ).add( subtree_depth_branching.get( i ).mean() );
			}
		}
		
		num_samples.add( tree_ground_size );
		min_max_samples.add( tree_ground_size );
	}
	
	public void visitActionNode( final FsssAbstractActionNode<S, A> aan )
	{
		final int increasing_depth = increasingDepth( aan.depth );
		if( subtree_depth_branching.size() <= increasing_depth ) {
			subtree_depth_branching.add( new MeanVarianceAccumulator() );
		}
//		subtree_depth_branching[increasing_depth] += aan.nsuccessors();
		subtree_depth_branching.get( increasing_depth ).add( aan.nsuccessors() );
//		subtree_stats.depth_branching.get( increasing_depth ).add( aan.nsuccessors() );
		
		for( final FsssAbstractStateNode<S, A> asn : aan.successors() ) {
			visitStateNode( asn );
		}
	}
	
	public void visitStateNode( final FsssAbstractStateNode<S, A> asn )
	{
		subtree_abstract_size += 1;
		tree_abstract_size += 1;
		subtree_ground_size += asn.states().size();
		tree_ground_size += asn.states().size();
		
		if( asn.isTerminal() || !asn.isExpanded() ) {
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
