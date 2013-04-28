/**
 * 
 */
package edu.oregonstate.eecs.mcplan.agents.galcon;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.search.BoundedVisitor;
import edu.oregonstate.eecs.mcplan.search.IterativeDeepeningSearch;
import edu.oregonstate.eecs.mcplan.search.NegamaxVisitor;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;
import edu.oregonstate.eecs.mcplan.util.Countdown;

/**
 * @author jhostetler
 *
 */
public class IterativeDeepeningPolicy<S, A extends UndoableAction<S, A>>
	implements AnytimePolicy<S, A>
{
	private final NegamaxVisitor<S, A> visitor_;
	private final int max_depth_;
	private final SimultaneousMoveSimulator<S, A> sim_;
	private final ActionGenerator<S, A> action_gen_;
	private final Policy<S, A> default_policy_;
	
	private S s_ = null;
	private long t_ = 0L;
	
	public IterativeDeepeningPolicy( final int max_depth,
									 final SimultaneousMoveSimulator<S, A> sim,
									 final ActionGenerator<S, A> action_gen,
									 final NegamaxVisitor<S, A> visitor,
									 final Policy<S, A> default_policy )
	{
		visitor_ = visitor;
		max_depth_ = max_depth;
		sim_ = sim;
		action_gen_ = action_gen;
		default_policy_ = default_policy;
	}

	@Override
	public void setState( final S s, final long t )
	{
		s_ = s;
		t_ = t;
	}

	@Override
	public A getAction()
	{
		return getAction( Long.MAX_VALUE );
	}

	@Override
	public void actionResult( final A a, final S sprime, final double r )
	{
		// TODO:
	}

	@Override
	public String getName()
	{
		return "IterativeDeepening";
	}

	@Override
	public long minControl()
	{
		return 0;
	}

	@Override
	public long maxControl()
	{
		return Long.MAX_VALUE;
	}

	@Override
	public A getAction( final long control )
	{
		System.out.println( "[IterativeDeepening] getAction()" );
		final BoundedVisitor<S, A> bv
			= new BoundedVisitor<S, A>( visitor_, new Countdown( control ) );
		final IterativeDeepeningSearch<S, A> search
			= new IterativeDeepeningSearch<S, A>( sim_, action_gen_, bv, max_depth_ );
		search.run();
		
		if( search.principalVariation() != null && search.principalVariation().actions.get( 0 ) != null ) {
			System.out.println( "[IterativeDeepening] PV: " + search.principalVariation() );
			return search.principalVariation().actions.get( 0 );
		}
		else {
			System.out.println( "[IterativeDeepening] ! Using leaf heuristic" );
			default_policy_.setState( s_, t_ );
			return default_policy_.getAction();
		}
	}
}
