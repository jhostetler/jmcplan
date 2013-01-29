/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.agents.galcon.ActionGenerator;
import edu.oregonstate.eecs.mcplan.agents.galcon.DurativeActionSimulator;
import edu.oregonstate.eecs.mcplan.agents.galcon.DurativeUndoableAction;
import edu.oregonstate.eecs.mcplan.agents.galcon.UndoableAction;
import edu.oregonstate.eecs.mcplan.agents.galcon.VariableDurationActionGenerator;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;
import edu.oregonstate.eecs.mcplan.util.F;

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
	
	/**
	 * Computes the optimal split for 'n' steps of discounting into 'depth'
	 * chunks. Here "optimal" means "minimal sum of absolute error". It is
	 * "minimal" in the sense that it's a greedy algorithm, so it minimizes
	 * intervals individually from front to back.
	 * @param n
	 * @param depth
	 * @return
	 */
	private int[] optimalSplit( final int n, final int depth )
	{
		final double[] y = makeDiscountList( discount_, n );
		final int[] idx = new int[depth + 1];
		Arrays.fill( idx, n );
		final double s = F.sum( y );
		idx[0] = 0;
		int p = idx[0];
		for( int i = 1; i < depth; ++i ) {
			double d = Math.abs( F.sum( F.slice( y, p, idx[i] ) ) - (s / depth) );
			while( idx[i] > p + 1 ) {
				idx[i] -= 1;
				final double dp = Math.abs( F.sum( F.slice( y, p, idx[i] ) ) - (s / depth) );
				if( dp > d ) {
					idx[i] += 1;
					break;
				}
				else {
					d = dp;
				}
			}
			p = idx[i];
		}
		idx[depth] = n;
		
		// Convert intervals to durations
		final int[] result = new int[depth];
		for( int i = 1; i < idx.length; ++i ) {
			result[i - 1] = idx[i] - idx[i - 1];
		}
		
		return result;
	}
	
	public PrincipalVariation<S, DurativeUndoableAction<S, A>> principalVariation()
	{
		return pv_;
	}
	
	@Override
	public void run()
	{
		System.out.println( "[DiscountedIterativeRefinementSearch] run(): max_depth_ = " + max_depth_ );
		int depth = 1;
		while( depth <= max_depth_ ) {
			final int[] idx = optimalSplit( Math.min( sim_.horizon(), max_horizon_ ), depth );
			if( idx[0] <= 0 ) {
				break; // A degenerate interval
			}
			System.out.println( "[DiscountedIterativeRefinementSearch] idx = " + Arrays.toString( idx ) );
			final DurativeActionSimulator<S, A> durative_sim = new DurativeActionSimulator<S, A>( sim_ );
			final DurativeNegamaxVisitor<S, A> durative_visitor = new DurativeNegamaxVisitor<S, A>( visitor_ );
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
				System.out.println( "[DiscountedIterativeRefinementSearch] Updating PV" );
			}
			System.out.println( "[DiscountedIterativeRefinementSearch] Depth: " + depth );
			System.out.println( "[DiscountedIterativeRefinementSearch] PV: " + pv_ );
			System.out.println( "[DiscountedIterativeRefinementSearch] Time: " + (stop - start) + " ms" );
			
			++depth;
		}
	}
}
