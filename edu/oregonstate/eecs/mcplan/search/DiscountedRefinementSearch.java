/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.DurativeUndoableAction;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.agents.galcon.VariableDurationActionGenerator;
import edu.oregonstate.eecs.mcplan.sim.DurativeActionSimulator;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;

/**
 * @author jhostetler
 *
 */
public class DiscountedRefinementSearch<S, A extends UndoableAction<S, A>> implements Runnable
{
	private final SimultaneousMoveSimulator<S, A> sim_;
	private final ActionGenerator<S, A> action_gen_;
	private final NegamaxVisitor<S, A> visitor_;
	private final int max_depth_;
	private final int max_horizon_;
	private final double discount_;
	
	private PrincipalVariation<S, DurativeUndoableAction<S, A>> pv_ = null;
	
	public DiscountedRefinementSearch( final SimultaneousMoveSimulator<S, A> sim,
									  final ActionGenerator<S, A> action_gen,
									  final NegamaxVisitor<S, A> visitor,
									  final int max_depth, final int max_horizon, final double discount )
	{
		sim_ = sim;
		action_gen_ = action_gen;
		visitor_ = visitor;
		max_depth_ = max_depth;
		max_horizon_ = max_horizon;
		discount_ = discount;
	}
	
	private double[] makeDiscountList( final double discount, final int n )
	{
		final double[] result = new double[n];
		result[0] = 1.0; // discount^0
		for( int i = 1; i < n; ++i ) {
			result[i] = result[i - 1] * discount;
		}
		return result;
	}
	
	private int[] optimalSplit( final double gamma, final int T, final int k )
	{
		assert( k >= 1 );
		assert( k <= T );
		if( k == 1 ) {
			return new int[] { T };
		}
		final double gammaT = Math.pow( gamma, T );
		final double logGamma = Math.log( gamma );
		final double logK = Math.log( k );
		final double[] real_result = new double[k];
		real_result[0] = (Math.log( gammaT + k - 1 ) - logK) / logGamma;
		for( int i = 1; i < k; ++i ) {
			real_result[i] = (Math.log( gammaT + k*Math.pow( gamma, real_result[i-1] ) - 1 ) - logK) / logGamma;
		}
		
		final int[] int_result = new int[k];
		int s = 0;
		for( int i = 0; i < k - 1; ++i ) {
			int_result[i] = (int) Math.ceil( real_result[i] );
			s += int_result[i];
		}
		int_result[k - 1] = T - s;
		
		// Distribute the error as evenly as possible while preserving
		// monotonicity.
		boolean progress = int_result[k - 1] < int_result[k - 2];
		while( progress ) {
			progress = false;
			for( int i = k - 2; i >= 1; --i ) {
				final int test = int_result[i] - 1;
				if( test > 0 && test >= int_result[i - 1] ) {
					int_result[i] -= 1;
					int_result[k - 1] += 1;
					progress = true;
					if( int_result[k - 1] >= int_result[k - 2] ) {
						break;
					}
				}
			}
		}
		return int_result;
	}
	
	/**
	 * Computes the optimal split for 'n' steps of discounting into 'depth'
	 * chunks. Here "optimal" means "minimal sum of absolute error". It is
	 * "minimal" in the sense that it's a greedy algorithm, so it minimizes
	 * intervals individually from front to back.
	 * @param n
	 * @param depth
	 * @return
	 */
//	private int[] optimalSplit( final long n, final int depth )
//	{
//		final double[] y = makeDiscountList( discount_, n );
//		final int[] idx = new int[depth + 1];
//		Arrays.fill( idx, (int) n );
////		final double s = F.sum( y );
//		idx[0] = 0;
//		int p = idx[0];
//		for( int i = 1; i < depth; ++i ) {
//			final double s = F.sum( F.slice( y, p, y.length ) );
//			final double r = s / (depth + 1 - i);
//			double d = Math.abs( F.sum( F.slice( y, p, idx[i] ) ) - r );
//			while( idx[i] > p + 1 ) {
//				idx[i] -= 1;
//				final double dp = Math.abs( F.sum( F.slice( y, p, idx[i] ) ) - r );
//				if( dp > d ) {
//					idx[i] += 1;
//					break;
//				}
//				else {
//					d = dp;
//				}
//			}
//			p = idx[i];
//		}
//		idx[depth] = (int) n;
//
//		// Convert intervals to durations
//		final int[] result = new int[depth];
//		for( int i = 1; i < idx.length; ++i ) {
//			result[i - 1] = idx[i] - idx[i - 1];
//		}
//
//		return result;
//	}
	
	public PrincipalVariation<S, DurativeUndoableAction<S, A>> principalVariation()
	{
		return pv_;
	}
	
	@Override
	public void run()
	{
		System.out.println( "[DiscountedRefinementSearch] run(): max_depth_ = " + max_depth_ );
		int depth = 1;
		while( depth <= max_depth_ ) {
			final long H = Math.min( sim_.horizon(), max_horizon_ );
			if( depth > H ) {
				break; // Stop searching at maximum granularity.
			}
			final int[] idx = optimalSplit( discount_, (int) H, depth );
			if( idx[0] <= 0 ) {
				break; // A degenerate interval
			}
			System.out.println( "[DiscountedRefinementSearch] idx = " + Arrays.toString( idx ) );
			final DurativeActionSimulator<S, A> durative_sim = new DurativeActionSimulator<S, A>( sim_ );
			final DurativeNegamaxVisitor<S, A> durative_visitor;
			if( pv_ == null ) {
				durative_visitor = new DurativeNegamaxVisitor<S, A>( visitor_ );
			}
			else {
				// Principal variation move ordering heuristic
				durative_visitor = new DurativePvMoveOrdering<S, A>( visitor_, pv_, idx );
			}
			final VariableDurationActionGenerator<S, A> durative_gen
				= new VariableDurationActionGenerator<S, A>( action_gen_, idx, durative_visitor );
			final NegamaxSearch<S, DurativeUndoableAction<S, A>> search
				= new NegamaxSearch<S, DurativeUndoableAction<S, A>>(
					durative_sim, depth * sim_.getNumAgents(), durative_gen, durative_visitor );
			final long start = System.currentTimeMillis();
			search.run();
			final long stop = System.currentTimeMillis();
			
			// TODO: Equivalent of PvMoveOrdering.
			if( search.principalVariation() != null
				&& (pv_ == null || search.isComplete()
					|| (search.principalVariation() != null
						&& search.principalVariation().isNarrowerThan( pv_ ))) ) {
				pv_ = search.principalVariation();
				System.out.println( "[DiscountedRefinementSearch] Updating PV" );
			}
			System.out.println( "[DiscountedRefinementSearch] Depth: " + depth );
			System.out.println( "[DiscountedRefinementSearch] PV: " + pv_ );
			System.out.println( "[DiscountedRefinementSearch] Time: " + (stop - start) + " ms" );
			
			if( !search.isComplete() ) {
				break;
			}
			
			++depth;
		}
	}
}
