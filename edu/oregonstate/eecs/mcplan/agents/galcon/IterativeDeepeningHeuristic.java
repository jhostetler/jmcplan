/**
 * 
 */
package edu.oregonstate.eecs.mcplan.agents.galcon;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.search.ForwardingNegamaxVisitor;
import edu.oregonstate.eecs.mcplan.search.IterativeDeepeningSearch;
import edu.oregonstate.eecs.mcplan.search.NegamaxVisitor;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;

/**
 * @author jhostetler
 *
 */
public class IterativeDeepeningHeuristic<S, A extends UndoableAction<S, A>>
	extends ForwardingNegamaxVisitor<S, A>
{
	private final int max_depth_;
	private final UndoSimulator<S, A> sim_;
	private final ActionGenerator<S, A> action_gen_;
	
	public IterativeDeepeningHeuristic( final NegamaxVisitor<S, A> inner,
										final int max_depth,
										final UndoSimulator<S, A> sim,
										final ActionGenerator<S, A> action_gen )
	{
		super( inner );
		max_depth_ = max_depth;
		sim_ = sim;
		action_gen_ = action_gen;
	}
	
	@Override
	public double heuristic( final S s )
	{
		System.out.println( "[IterativeDeepeningHeuristic] heuristic()" );
		final IterativeDeepeningSearch<S, A> search
			= new IterativeDeepeningSearch<S, A>( sim_, action_gen_, inner_, max_depth_ );
		search.run();
		
		if( search.principalVariation() != null ) {
			System.out.println( "[IterativeDeepeningHeuristic] PV: " + search.principalVariation() );
			// Heuristic value is score of principal variation
			return search.principalVariation().score;
		}
		else {
			System.out.println( "[IterativeDeepeningHeuristic] ! Using leaf heuristic" );
			return inner_.heuristic( s );
		}
	}
	
}
