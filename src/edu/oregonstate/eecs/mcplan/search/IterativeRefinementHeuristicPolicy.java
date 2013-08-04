/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;
import edu.oregonstate.eecs.mcplan.util.Countdown;

/**
 * @author jhostetler
 *
 */
public class IterativeRefinementHeuristicPolicy<S, A extends UndoableAction<S, A>>
	implements AnytimePolicy<S, A>
{
	private final int max_depth_;
	private final int max_horizon_;
	private final SimultaneousMoveSimulator<S, A> sim_;
	private final ActionGenerator<S, A> action_gen_;
	private final NegamaxVisitor<S, A> visitor_;
	private final AnytimePolicy<S, A> default_policy_;
	
	private S s_ = null;
	private long t_ = 0L;
	
	/**
	 * @param switch_epoch
	 */
	public IterativeRefinementHeuristicPolicy(
			final int max_depth, final int max_horizon,
			final SimultaneousMoveSimulator<S, A> sim,
			final ActionGenerator<S, A> action_gen,
			final NegamaxVisitor<S, A> visitor,
			final AnytimePolicy<S, A> default_policy )
	{
		max_depth_ = max_depth;
		max_horizon_ = max_horizon;
		sim_ = sim;
		action_gen_ = action_gen;
		visitor_ = visitor;
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
		return getAction( maxControl() );
	}

	@Override
	public void actionResult( final A a, final S sprime, final double r )
	{ }

	@Override
	public String getName()
	{
		return "IterativeRefinementHeuristic";
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
		final BoundedVisitor<S, A> bv
			= new BoundedVisitor<S, A>( visitor_, new Countdown( control ) );
		final IterativeRefinementHeuristic<S, A> heuristic
			= new IterativeRefinementHeuristic<S, A>( bv, max_depth_, max_horizon_, sim_, action_gen_.create() );
		final NegamaxSearch<S, A> search = new NegamaxSearch<S, A>(
			sim_, sim_.getNumAgents() /* One level */, action_gen_, heuristic );
		System.out.println( "*** Running root search" );
		search.run();
		System.out.println( "*** Root search done" );
		
		if( search.principalVariation() == null ) {
			System.out.println( "[IterativeRefinementPolicy] ! search.principalVariation() = NULL" );
		}
		else if( search.principalVariation().actions.get( 0 ) == null ) {
			System.out.println( "[IterativeRefinementPolicy] ! First action is NULL" );
		}
		
		if( search.principalVariation() != null && search.principalVariation().actions.get( 0 ) != null ) {
			// Choose the first action in the best PV.
			return search.principalVariation().actions.get( 0 );
		}
		else {
			System.out.println( "[IterativeRefinementPolicy] ! Using default policy" );
			assert( s_ != null );
			default_policy_.setState( s_, t_ );
			final A a = default_policy_.getAction( control );
			// FIXME: How do we get actionResult() to the default policy (or do we)?
			return a;
		}
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "IR_" ).append( max_depth_ ).append( "_" ).append( max_horizon_ );
		return sb.toString();
	}
	
	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof IterativeRefinementHeuristicPolicy) ) {
			return false;
		}
		final IterativeRefinementHeuristicPolicy<?, ?> that
			= (IterativeRefinementHeuristicPolicy<?, ?>) obj;
		return toString().equals( that.toString() );
	}
}
