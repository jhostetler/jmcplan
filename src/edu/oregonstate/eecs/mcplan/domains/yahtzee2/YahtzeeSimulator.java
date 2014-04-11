/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;

/**
 * @author jhostetler
 *
 */
public class YahtzeeSimulator implements UndoSimulator<YahtzeeState, YahtzeeAction>
{
	private final YahtzeeState s_;
	
	private final int Horizon_ = YahtzeeScores.values().length;
	
	private final Deque<JointAction<YahtzeeAction>> action_history_
		= new ArrayDeque<JointAction<YahtzeeAction>>();
	
	public YahtzeeSimulator( final RandomGenerator rng )
	{
		s_ = new YahtzeeState( rng );
		
		// We roll an initial hand, so that rolling dice can be the last step
		// of KeepAction and ScoreAction, and thus we don't need a 'RollDice'
		// action, which would be the only action available whenever it is
		// legal.
		s_.setHand( new Hand( s_.roll( Hand.Ndice ) ), Hand.Nrerolls );
	}
	
	@Override
	public YahtzeeState state()
	{
		return s_;
	}

	@Override
	public void takeAction( final JointAction<YahtzeeAction> a )
	{
		assert( a.nagents == 1 );
		a.get( 0 ).doAction( s_ );
		action_history_.push( a );
	}

	@Override
	public void untakeLastAction()
	{
		final JointAction<YahtzeeAction> a = action_history_.pop();
		a.get( 0 ).undoAction( s_ );
	}

	@Override
	public long depth()
	{
		return action_history_.size();
	}

	@Override
	public long t()
	{
		return action_history_.size();
	}

	@Override
	public int nagents()
	{
		return 1;
	}

	@Override
	public int[] turn()
	{
		return new int[] { 0 };
	}

	@Override
	public double[] reward()
	{
		if( s_.isTerminal() ) {
			return new double[] { s_.score() };
		}
		else {
			return new double[] { 0 };
		}
	}

	@Override
	public boolean isTerminalState()
	{
		return s_.isTerminal();
	}

	@Override
	public long horizon()
	{
		return Horizon_ - depth();
	}

	@Override
	public String detailString()
	{
		return "YahtzeeSimulator";
	}

}
