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
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * A GameTreeVisitor that records useful statistics about the tree. You can
 * apply the same TreeStatisticsRecorder instance to a batch of trees to
 * compute statistics averaged over the batch.
 */
public class TreeStatisticsRecorder<S, A extends VirtualConstructor<A>> implements GameTreeVisitor<S, A>
{
	/**
	 * Accumulates *relative* state branching, which is number of distinct
	 * successor states divided by number of trials of the parent action. A
	 * value close to 1.0 indicates that duplicate states are rarely
	 * encountered, while values tending toward 0 indicate more determinism.
	 */
	public final MeanVarianceAccumulator state_branching = new MeanVarianceAccumulator();
	
	/**
	 * Accumulates the *absolute* number of different actions tried per state.
	 * This statistic seems practically useless, since it will be dominated
	 * by the leaf nodes.
	 */
	public final MeanVarianceAccumulator action_branching = new MeanVarianceAccumulator();
	
	/**
	 * Accumulates the average maximum tree depth.
	 */
	public final MeanVarianceAccumulator depth = new MeanVarianceAccumulator();
	
	/**
	 * Accumulates the average total number of action node visits.
	 */
	public final MeanVarianceAccumulator action_visits = new MeanVarianceAccumulator();
	
	/**
	 * Accumulates the average depth of leaf nodes.
	 */
	public final MeanVarianceAccumulator avg_depth = new MeanVarianceAccumulator();
	
	private int depth_ = 0;
	private int max_depth_ = 0;
	private int Naction_visits_ = 0;
	private MeanVarianceAccumulator leaf_depth_ = new MeanVarianceAccumulator();
	
	private void finishTree()
	{
		depth.add( max_depth_ );
		action_visits.add( Naction_visits_ );
		avg_depth.add( leaf_depth_.mean() );
		
		max_depth_ = 0;
		Naction_visits_ = 0;
		leaf_depth_ = new MeanVarianceAccumulator();
	}
	
	private void visitPrefix()
	{
		depth_ += 1;
		if( depth_ > max_depth_ ) {
			max_depth_ = depth_;
		}
	}
	
	private void visitSuffix()
	{
		depth_ -= 1;
		if( depth_ == 0 ) {
			finishTree();
		}
	}
	
	@Override
	public void visit( final StateNode<S, A> s )
	{
		visitPrefix();
		
		if( s.n() > 0 ) {
			// FIXME: This can't be tail-recursive because Generator doesn't
			// keep track of its size.
			int count = 0;
			for( final ActionNode<S, A> an : Fn.in( s.successors() ) ) {
				visit( an );
				count += 1;
			}
			action_branching.add( count );
			
			if( count == 0 ) {
				// It's a leaf
				leaf_depth_.add( depth_ );
			}
		}
		
		visitSuffix();
	}

	@Override
	public void visit( final ActionNode<S, A> a )
	{
		visitPrefix();
		
		if( a.n() > 0 ) {
			// FIXME: This can't be tail-recursive because Generator doesn't
			// keep track of its size.
			int count = 0;
			for( final StateNode<S, A> sn : Fn.in( a.successors() ) ) {
				visit( sn );
	//			if( ((ClusterAbstraction) sn.token).cluster_ >= 0 ) {
	//				count += 1;
	//			}
				count += 1;
			}
			
			// If a.n() == 1, we don't count it because the branching factor
			// will always be 1.0.
			if( a.n() > 1 ) {
				state_branching.add( ((double) count) / a.n() );
			}
			
			if( count == 0 ) {
				// It's a leaf
				leaf_depth_.add( depth_ );
			}
		}
		
		Naction_visits_ += a.n();
		
		visitSuffix();
	}
}
