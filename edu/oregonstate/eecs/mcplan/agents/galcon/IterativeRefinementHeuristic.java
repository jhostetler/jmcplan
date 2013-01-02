/**
 * 
 */
package edu.oregonstate.eecs.mcplan.agents.galcon;

import edu.oregonstate.eecs.mcplan.search.FastGameTreeNegamax;
import edu.oregonstate.eecs.mcplan.search.ForwardingNegamaxVisitor;
import edu.oregonstate.eecs.mcplan.search.NegamaxVisitor;
import edu.oregonstate.eecs.mcplan.search.NegamaxVisitorBase;
import edu.oregonstate.eecs.mcplan.search.PrincipalVariation;
import edu.oregonstate.eecs.mcplan.util.Pointer;

/**
 * @author jhostetler
 *
 */
public class IterativeRefinementHeuristic<S, A extends UndoableAction<S, A>>
	extends ForwardingNegamaxVisitor<S, A>
{
	private class BoundedDurativeVisitor extends NegamaxVisitorBase<S, DurativeUndoableAction<S, A>>
	{
		public long node_count;
		public long node_limit;
		
		private final Pointer<PrincipalVariation<S, DurativeUndoableAction<S, A>>> pv_ref_;
		
		public BoundedDurativeVisitor( final Pointer<PrincipalVariation<S, DurativeUndoableAction<S, A>>> pv_ref,
							   		   final long node_count, final long node_limit )
		{
			pv_ref_ = pv_ref;
			this.node_count = node_count;
			this.node_limit = node_limit;
		}
		
		@Override
		public void discoverVertex( final S s )
		{
			++node_count;
			if( node_count >= node_limit ) {
				throw new NodeLimitException();
			}
			inner_.discoverVertex( s );
		}
		
		@Override
		public void principalVariation( final PrincipalVariation<S, DurativeUndoableAction<S, A>> pv )
		{
			if( pv_ref_.get() == null || pv.score > pv_ref_.get().score ) {
				pv_ref_.set( pv );
			}
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final int switch_epoch_;
	private final int max_depth_;
	private final int max_horizon_;
	private final int max_nodes_;
	private final UndoSimulator<S, A> sim_;
	private final ActionGenerator<S, A> action_gen_;
	
	public IterativeRefinementHeuristic( final NegamaxVisitor<S, A> inner,
										 final int switch_epoch, final int max_depth,
										 final int max_horizon, final int max_nodes,
										 final UndoSimulator<S, A> sim,
										 final ActionGenerator<S, A> action_gen )
	{
		super( inner );
		switch_epoch_ = switch_epoch;
		max_depth_ = max_depth;
		max_horizon_ = max_horizon;
		max_nodes_ = max_nodes;
		sim_ = sim;
		action_gen_ = action_gen;
	}
	
	@Override
	public double heuristic( final S s )
	{
		System.out.println( "Iterative refinement heuristic" );
		long node_count = 0;
		PrincipalVariation<S, DurativeUndoableAction<S, A>> overall_best_pv = null;
		Pointer<PrincipalVariation<S, DurativeUndoableAction<S, A>>> depth_best_pv = null;
		try {
			int depth = 1;
			while( depth <= max_depth_ ) {
				final int ply_depth = depth * sim_.getNumAgents();
				depth_best_pv = new Pointer<PrincipalVariation<S, DurativeUndoableAction<S, A>>>();
				final BoundedDurativeVisitor bv
					= new BoundedDurativeVisitor( depth_best_pv, node_count, max_nodes_ );
				final int policy_epoch = Math.min( sim_.horizon(), max_horizon_ ) / ply_depth;
				final DurativeActionSimulator<S, A> durative_sim
					= new DurativeActionSimulator<S, A>( sim_, policy_epoch );
				final FastGameTreeNegamax<S, DurativeUndoableAction<S, A>> search
					= new FastGameTreeNegamax<S, DurativeUndoableAction<S, A>>(
						durative_sim, ply_depth, -Double.MAX_VALUE, Double.MAX_VALUE,
						new DurativeActionGenerator<S, A>( action_gen_, policy_epoch ), bv );
		//				new NegamaxVisitorBase<GalconState, GalconAction>() );
		//				new LoggingNegamaxVisitor<GalconState, GalconAction>( System.out ) );
				final long start = System.currentTimeMillis();
				search.run();
				final long stop = System.currentTimeMillis();
				System.out.println( "Depth: " + depth );
				System.out.println( "Ply depth: " + ply_depth );
				System.out.println( "PV: " + depth_best_pv.get() );
				System.out.println( "Time: " + (stop - start) + " ms" );
				node_count = bv.node_count;
				System.out.println( "Node count: " + node_count );
				assert( depth_best_pv.get() != null );
				overall_best_pv = depth_best_pv.get();
				++depth;
			}
			System.out.println( "Depth limit" );
		}
		catch( final NodeLimitException ex ) {
			System.out.println( "Node limit" );
			if( depth_best_pv.get() != null ) {
				overall_best_pv = depth_best_pv.get();
			}
		}
		// Heuristic value is score of principal variation
		assert( overall_best_pv != null );
		return overall_best_pv.score;
	}
	
}
