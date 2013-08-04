/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Countdown;

/**
 * @author jhostetler
 *
 */
public class IterativeDeepeningHeuristicPolicy<S, A extends UndoableAction<S, A>>
	implements AnytimePolicy<S, A>
{
	private final int max_depth_;
	private final UndoSimulator<S, A> sim_;
	private final ActionGenerator<S, A> action_gen_;
	private final NegamaxVisitor<S, A> visitor_;
	private final AnytimePolicy<S, A> default_policy_;
	
	/**
	 * @param switch_epoch
	 */
	public IterativeDeepeningHeuristicPolicy(
			final int max_depth,
			final UndoSimulator<S, A> sim,
			final ActionGenerator<S, A> action_gen,
			final NegamaxVisitor<S, A> visitor,
			final AnytimePolicy<S, A> default_policy )
	{
		max_depth_ = max_depth;
		sim_ = sim;
		action_gen_ = action_gen;
		visitor_ = visitor;
		default_policy_ = default_policy;
	}

	@Override
	public String getName()
	{
		return "IterativeDeepeningHeuristic";
	}

	@Override
	public long minControl()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long maxControl()
	{
		return Long.MAX_VALUE;
	}
	
	@Override
	public void setState( final S s, final long t )
	{ }

	@Override
	public A getAction()
	{
		return getAction( maxControl() );
	}
	
	// -----------------------------------------------------------------------

	@Override
	public A getAction( final long control )
	{
		final BoundedVisitor<S, A> bv = new BoundedVisitor<S, A>( visitor_, new Countdown( control ) );
		final IterativeDeepeningHeuristic<S, A> heuristic
			= new IterativeDeepeningHeuristic<S, A>( bv, max_depth_, sim_, action_gen_.create() );
		final NegamaxSearch<S, A> search = new NegamaxSearch<S, A>(
			sim_, sim_.getNumAgents() /* One level at normal time discretization */,
			action_gen_.create(), heuristic );
		search.run();
		
		if( search.principalVariation() != null && search.principalVariation().actions.get( 0 ) != null ) {
			// Choose the first action in the best PV.
			return search.principalVariation().actions.get( 0 );
		}
		else {
			default_policy_.setState( sim_.state(), sim_.depth() );
			return default_policy_.getAction();
		}
	}
	
	// -----------------------------------------------------------------------

	@Override
	public void actionResult( final A a, final S sprime, final double r )
	{ }
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "ID_" ).append( max_depth_ );
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
		if( obj == null || !(obj instanceof IterativeDeepeningHeuristicPolicy) ) {
			return false;
		}
		final IterativeDeepeningHeuristicPolicy<?, ?> that
			= (IterativeDeepeningHeuristicPolicy<?, ?>) obj;
		return toString().equals( that.toString() );
	}

}
