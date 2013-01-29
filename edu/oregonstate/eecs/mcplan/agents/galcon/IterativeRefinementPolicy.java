/**
 * 
 */
package edu.oregonstate.eecs.mcplan.agents.galcon;

import edu.oregonstate.eecs.mcplan.search.IterativeRefinementSearch;
import edu.oregonstate.eecs.mcplan.search.NegamaxVisitor;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;
import edu.oregonstate.eecs.mcplan.util.Countdown;

/**
 * @author jhostetler
 *
 */
public class IterativeRefinementPolicy<S, A extends UndoableAction<S, A>>
	implements AnytimePolicy<S, A>
{
	private final NegamaxVisitor<S, A> visitor_;
	private final int max_depth_;
	private final int max_horizon_;
	private final SimultaneousMoveSimulator<S, A> sim_;
	private final ActionGenerator<S, A> action_gen_;
	private final Policy<S, A> default_policy_;
	
	private S s_ = null;
	private Policy<S, A> policy_used_ = null;
	
	public IterativeRefinementPolicy( final int max_depth,
									  final int max_horizon,
									  final SimultaneousMoveSimulator<S, A> sim,
									  final ActionGenerator<S, A> action_gen,
									  final NegamaxVisitor<S, A> visitor,
									  final Policy<S, A> default_policy )
	{
		visitor_ = visitor;
		max_depth_ = max_depth;
		max_horizon_ = max_horizon;
		sim_ = sim;
		action_gen_ = action_gen;
		default_policy_ = default_policy;
	}

	@Override
	public void setState( final S s )
	{
		s_ = s;
	}

	@Override
	public A getAction()
	{
		return getAction( Long.MAX_VALUE );
	}

	@Override
	public void actionResult( final A a, final S sprime, final double r )
	{
		assert( policy_used_ != null );
		policy_used_.actionResult( a, sprime, r );
	}

	@Override
	public String getName()
	{
		return "DirectIterativeRefinement";
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
		System.out.println( "[IterativeRefinementPolicy] getAction()" );
		final BoundedVisitor<S, A> bv
			= new BoundedVisitor<S, A>( visitor_, new Countdown( control ) );
		final IterativeRefinementSearch<S, A> search
			= new IterativeRefinementSearch<S, A>( sim_, action_gen_, bv, max_depth_, max_horizon_ );
		search.run();
		
		if( search.principalVariation() != null && search.principalVariation().actions.get( 0 ) != null ) {
			System.out.println( "[IterativeRefinementPolicy] PV: " + search.principalVariation() );
			policy_used_ = search.principalVariation().actions.get( 0 ).policy_;
		}
		else {
			System.out.println( "[IterativeRefinementPolicy] ! Using leaf heuristic" );
			policy_used_ = default_policy_;
		}
		
		policy_used_.setState( s_ );
		return policy_used_.getAction();
	}
}
