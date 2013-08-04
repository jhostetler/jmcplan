/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import java.util.ArrayDeque;
import java.util.Deque;

import edu.oregonstate.eecs.mcplan.DurativeAction;
import edu.oregonstate.eecs.mcplan.UndoableAction;



/**
 * @author jhostetler
 *
 */
public class DurativeActionSimulator<S, A extends UndoableAction<S>>
	implements UndoSimulator<S, DurativeAction<S, A>>
{
	private final SimultaneousMoveSimulator<S, A> base_sim_;
	private final Deque<Integer> epochs_ = new ArrayDeque<Integer>();
	
	public DurativeActionSimulator( final SimultaneousMoveSimulator<S, A> base_sim )
	{
		super( base_sim.getNumAgents() );
		base_sim_ = base_sim;
		setTurn( base_sim_.getTurn() );
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
		return base_sim_.horizon(); // FIXME: How do we implement this without a fixed epoch? / epoch_;
	}
	
	@Override
	public void setTurn( final int turn )
	{
		System.out.println( "[DurativeActionSimulator] setTurn( " + turn + " )" );
		super.setTurn( turn );
		base_sim_.setTurn( turn );
	}

	@Override
	protected void advance()
	{
		final int epoch = action_history_.peek().get( 0 ).T_;
		// TODO: Debugging
		for( final DurativeAction<S> ai : action_history_.peek() ) {
			if( ai.T_ != epoch ) {
				System.out.println( "ai.T_ = " + ai.T_ + "; epoch = " + epoch );
			}
			assert( ai.T_ == epoch );
		}
		epochs_.push( epoch );
		
		for( int t = 0; t < epoch; ++t ) {
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
		final int epoch = epochs_.pop();
		
		for( int t = 0; t < epoch; ++t ) {
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
