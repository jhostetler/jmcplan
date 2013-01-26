/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.agents.galcon.ActionGenerator;
import edu.oregonstate.eecs.mcplan.agents.galcon.DurativeActionGenerator;
import edu.oregonstate.eecs.mcplan.agents.galcon.DurativeActionSimulator;
import edu.oregonstate.eecs.mcplan.agents.galcon.DurativeUndoableAction;
import edu.oregonstate.eecs.mcplan.agents.galcon.UndoableAction;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;

/**
 * @author jhostetler
 *
 */
public class IterativeRefinementSearch<S, A extends UndoableAction<S, A>>
	implements GameTreeSearch<S, DurativeUndoableAction<S, A>>
{
	private final SimultaneousMoveSimulator<S, A> sim_;
	private final ActionGenerator<S, A> action_gen_;
	private final NegamaxVisitor<S, A> visitor_;
	private final int max_depth_;
	private final int max_horizon_;
	
	private PrincipalVariation<S, DurativeUndoableAction<S, A>> pv_ = null;
	private boolean complete_ = false;
	
	public IterativeRefinementSearch( final SimultaneousMoveSimulator<S, A> sim,
									  final ActionGenerator<S, A> action_gen,
									  final NegamaxVisitor<S, A> visitor,
									  final int max_depth, final int max_horizon )
	{
		sim_ = sim;
		action_gen_ = action_gen;
		visitor_ = visitor;
		max_depth_ = max_depth;
		max_horizon_ = max_horizon;
	}
	
	@Override
	public double score()
	{
		return pv_.score;
	}
	
	@Override
	public PrincipalVariation<S, DurativeUndoableAction<S, A>> principalVariation()
	{
		return pv_;
	}
	
	@Override
	public boolean isComplete()
	{
		return complete_;
	}
	
	@Override
	public void run()
	{
		System.out.println( "[IterativeRefinementSearch] run(): max_depth_ = " + max_depth_ );
		int depth = 1;
		while( depth <= max_depth_ ) {
			final int policy_epoch = Math.min( sim_.horizon(), max_horizon_ ) / depth;
			final DurativeActionSimulator<S, A> durative_sim
				= new DurativeActionSimulator<S, A>( sim_ );
			final DurativeActionGenerator<S, A> durative_gen
				= new DurativeActionGenerator<S, A>( action_gen_, policy_epoch );
//			final IterativeDeepeningSearch<S, DurativeUndoableAction<S, A>> search
//				= new IterativeDeepeningSearch<S, DurativeUndoableAction<S, A>>(
//					durative_sim, durative_gen.create(),
//					new DurativeNegamaxVisitor<S, A>( visitor_, policy_epoch ), depth );
			final NegamaxSearch<S, DurativeUndoableAction<S, A>> search
				= new NegamaxSearch<S, DurativeUndoableAction<S, A>>(
					durative_sim, depth * sim_.getNumAgents(), durative_gen.create(),
					new DurativeNegamaxVisitor<S, A>( visitor_ ) );
			final long start = System.currentTimeMillis();
			search.run();
			final long stop = System.currentTimeMillis();
			
			// TODO: Equivalent of PvMoveOrdering.
			if( search.principalVariation() != null
				&& (pv_ == null || search.isComplete()
					|| (search.principalVariation() != null
						&& search.principalVariation().isNarrowerThan( pv_ ))) ) {
				pv_ = search.principalVariation();
				System.out.println( "[IterativeRefinementSearch] Updating PV" );
			}
			System.out.println( "[IterativeRefinementSearch] Depth: " + depth );
			System.out.println( "[IterativeRefinementSearch] PV: " + pv_ );
			System.out.println( "[IterativeRefinementSearch] Time: " + (stop - start) + " ms" );
			
			++depth;
		}
		complete_ = true;
	}
}
