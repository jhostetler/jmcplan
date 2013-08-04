/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import edu.oregonstate.eecs.mcplan.DurativeAction;
import edu.oregonstate.eecs.mcplan.UndoableAction;

/**
 * An adapter that "coarsens" the time resolution of a base simulator. The
 * primitive time steps in the adapter each correspond to a constant number
 * of time steps in the base simulator.
 * 
 * Although superficially similar to DurativeActionSimulator, this class
 * is semantically quite different. Namely, CoarseSimulation represents
 * reducing the time granularity of the simulation, whereas
 * DurativeActionSimulator enables agents to *choose* how long to execute
 * an action for. The fact that both of them use DurativeUndoableAction is
 * an implementation convenience.
 * 
 * TODO: Should there be a different class besides DurativeUndoableAction,
 * to clarify the semantic distinction?
 * 
 * @author jhostetler
 *
 */
public class CoarseSimulation<S, A extends UndoableAction<S, A>>
	extends SimultaneousMoveSimulator<S, DurativeAction<S, A>>
{
	public static <S, A extends UndoableAction<S, A>>
	CoarseSimulation<S, A> create( final SimultaneousMoveSimulator<S, A> base_sim, final int epoch )
	{
		return new CoarseSimulation<S, A>( base_sim, epoch );
	}
	
	private final SimultaneousMoveSimulator<S, A> base_sim_;
	private final int epoch_;
	
	public CoarseSimulation( final SimultaneousMoveSimulator<S, A> base_sim, final int epoch )
	{
		super( base_sim.getNumAgents() );
		base_sim_ = base_sim;
		epoch_ = epoch;
	}
	
	@Override
	public S state()
	{
		return base_sim_.state();
	}
	
	@Override
	public int getNumAgents()
	{
		return base_sim_.getNumAgents();
	}

	@Override
	public double getReward()
	{
		return base_sim_.getReward();
	}

	@Override
	public String detailString()
	{
		return base_sim_.detailString();
	}

	@Override
	public long horizon()
	{
		// FIXME: This should probably be ceil( ... )
		return base_sim_.horizon() / epoch_;
	}
	
	@Override
	public void setTurn( final int turn )
	{
		System.out.println( "[CoarseSimulation] setTurn( " + turn + " )" );
		super.setTurn( turn );
		base_sim_.setTurn( turn );
	}

	@Override
	protected void advance()
	{
		for( final DurativeAction<S, A> ai : action_history_.peek() ) {
			assert( ai.T_ == epoch_ );
		}
		
		for( int t = 0; t < epoch_; ++t ) {
			for( final DurativeAction<S, A> ai : action_history_.peek() ) {
				final DurativeAction<S, A> cp = ai.create();
				cp.policy_.setState( state(), base_sim_.depth() );
				final A policy_action = cp.policy_.getAction();
//					System.out.println( policy_action );
				base_sim_.takeAction( policy_action );
				final double r = base_sim_.getReward();
				cp.policy_.actionResult( policy_action, state(), r );
			}
		}
	}

	@Override
	protected void unadvance()
	{
		for( int t = 0; t < epoch_; ++t ) {
			for( int i = 0; i < action_history_.peek().size(); ++i ) {
				base_sim_.untakeLastAction();
			}
		}
	}
	
	@Override
	public String toString()
	{
		// XXX: Other code depends on the format of toString().
		// Don't change for now!
		return "[d: " + base_sim_.depth() + ", p: " + getTurn() + "]";
	}

	@Override
	public boolean isTerminalState( final S s )
	{
		return base_sim_.isTerminalState( s );
	}
}
