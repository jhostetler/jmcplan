/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;

/**
 * @author jhostetler
 *
 */
public class IterativeRefinementHeuristic<S, A extends UndoableAction<S, A>>
	extends ForwardingNegamaxVisitor<S, A>
{
	private final int max_depth_;
	private final int max_horizon_;
	private final SimultaneousMoveSimulator<S, A> sim_;
	private final ActionGenerator<S, A> action_gen_;
	
	public IterativeRefinementHeuristic( final NegamaxVisitor<S, A> inner,
										 final int max_depth,
										 final int max_horizon,
										 final SimultaneousMoveSimulator<S, A> sim,
										 final ActionGenerator<S, A> action_gen )
	{
		super( inner );
		max_depth_ = max_depth;
		max_horizon_ = max_horizon;
		sim_ = sim;
		action_gen_ = action_gen;
	}
	
	@Override
	public double heuristic( final S s )
	{
		System.out.println( "[IterativeRefinementHeuristic] heuristic()" );
		final IterativeRefinementSearch<S, A> search
			= new IterativeRefinementSearch<S, A>( sim_, action_gen_, inner_, max_depth_, max_horizon_ );
		search.run();
		
		if( search.principalVariation() != null ) {
			System.out.println( "[IterativeRefinementHeuristic] PV: " + search.principalVariation() );
			// Heuristic value is score of principal variation
			return search.principalVariation().score;
		}
		else {
			System.out.println( "[IterativeRefinementHeuristic] ! Using leaf heuristic" );
			return inner_.heuristic( s );
		}
	}
	
}
