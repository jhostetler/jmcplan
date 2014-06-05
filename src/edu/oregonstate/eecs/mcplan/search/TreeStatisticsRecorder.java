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
public class TreeStatisticsRecorder<X, A extends VirtualConstructor<A>> implements GameTreeVisitor<X, A>
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
	 * Accumulates the maximum tree depth.
	 */
	public final MeanVarianceAccumulator depth = new MeanVarianceAccumulator();
	
	private int depth_ = 0;
	private int max_depth_ = 0;
	
	@Override
	public void visit( final StateNode<X, A> s )
	{
		depth_ += 1;
		if( depth_ > max_depth_ ) {
			max_depth_ = depth_;
		}
		
		if( s.n() > 0 ) {
			// FIXME: This can't be tail-recursive because Generator doesn't
			// keep track of its size.
			int count = 0;
			for( final ActionNode<X, A> an : Fn.in( s.successors() ) ) {
				visit( an );
				count += 1;
			}
			action_branching.add( count );
		}
		
		depth_ -= 1;
		if( depth_ == 0 ) {
			depth.add( max_depth_ );
		}
	}

	@Override
	public void visit( final ActionNode<X, A> a )
	{
		depth_ += 1;
		if( depth_ > max_depth_ ) {
			max_depth_ = depth_;
		}
		
		// If a.n() == 1, we don't count it because the branching factor
		// will always be 1.0.
		if( a.n() > 1 ) {
			// FIXME: This can't be tail-recursive because Generator doesn't
			// keep track of its size.
			int count = 0;
			for( final StateNode<X, A> sn : Fn.in( a.successors() ) ) {
				visit( sn );
	//			if( ((ClusterAbstraction) sn.token).cluster_ >= 0 ) {
	//				count += 1;
	//			}
				count += 1;
			}
			state_branching.add( ((double) count) / a.n() );
		}
		
		depth_ -= 1;
		if( depth_ == 0 ) {
			depth.add( max_depth_ );
			max_depth_ = 0;
		}
	}
}
