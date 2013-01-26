/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.agents.galcon.ActionGenerator;
import edu.oregonstate.eecs.mcplan.agents.galcon.UndoSimulator;
import edu.oregonstate.eecs.mcplan.agents.galcon.UndoableAction;

/**
 * @author jhostetler
 *
 */
public class IterativeDeepeningSearch<S, A extends UndoableAction<S, A>> implements GameTreeSearch<S, A>
{
	private final UndoSimulator<S, A> sim_;
	private final ActionGenerator<S, A> action_gen_;
	private final NegamaxVisitor<S, A> visitor_;
	private final int max_depth_;
	
	private PrincipalVariation<S, A> pv_ = null;
	private boolean complete_ = false;
	
	public IterativeDeepeningSearch( final UndoSimulator<S, A> sim, final ActionGenerator<S, A> action_gen,
									 final NegamaxVisitor<S, A> visitor, final int max_depth )
	{
		sim_ = sim;
		action_gen_ = action_gen;
		visitor_ = visitor;
		max_depth_ = max_depth;
	}
	
	@Override
	public double score()
	{
		return pv_.score;
	}
	
	@Override
	public PrincipalVariation<S, A> principalVariation()
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
		System.out.println( "[IterativeDeepeningSearch] run(): max_depth_ = " + max_depth_ );
		ActionGenerator<S, A> local_gen = action_gen_.create();
		int depth = 1;
		while( depth <= max_depth_ ) {
			final NegamaxSearch<S, A> search = new NegamaxSearch<S, A>(
				sim_, depth * sim_.getNumAgents(), local_gen, visitor_ );
			final long start = System.currentTimeMillis();
			search.run();
			final long stop = System.currentTimeMillis();
			
			System.out.println( "[IterativeDeepeningSearch] search.isComplete() = " + search.isComplete() );
			if( search.principalVariation() != null
				&& (pv_ == null || search.isComplete()
					|| (search.principalVariation() != null
						&& search.principalVariation().isNarrowerThan( pv_ ))) ) {
				if( pv_ == null ) {
					System.out.println( "[IterativeDeepeningSearch] pv_ was NULL" );
				}
				else {
					System.out.println( "[IterativeDeepeningSearch] Updating PV ("
										+ pv_.alpha + ", " + pv_.beta + ") -> ("
										+ search.principalVariation().alpha + ", "
										+ search.principalVariation().beta + ")" );
				}
				pv_ = search.principalVariation();
				local_gen = new PvMoveOrdering<S, A>( action_gen_, pv_ );
			}
			else {
				if( search.principalVariation() != null ) {
					System.out.println( "[IterativeDeepeningSearch] Not updating PV ("
										+ pv_.alpha + ", " + pv_.beta + ") -> ("
										+ search.principalVariation().alpha + ", "
										+ search.principalVariation().beta + ")" );
				}
				else if( pv_ != null ) {
					System.out.println( "[IterativeDeepeningSearch] Not updating PV ("
										+ pv_.alpha + ", " + pv_.beta + ") -> NULL" );
				}
				else {
					System.out.println( "[IterativeDeepeningSearch] Not updating PV NULL -> NULL" );
				}
				local_gen = action_gen_.create();
			}
			System.out.println( "[IterativeDeepeningSearch] Depth: " + depth );
			System.out.println( "[IterativeDeepeningSearch] PV: " + pv_ );
			System.out.println( "[IterativeDeepeningSearch] Time: " + (stop - start) + " ms" );
			
			++depth;
		}
		complete_ = true;
	}
}
