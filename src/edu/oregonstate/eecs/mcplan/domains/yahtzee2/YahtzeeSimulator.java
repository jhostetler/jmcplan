/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import java.util.ArrayDeque;
import java.util.Deque;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

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
	
	private final TIntStack scores_ = new TIntArrayStack();
	
	public YahtzeeSimulator( final YahtzeeState s )
	{
		s_ = s;
		scores_.push( s_.score() );
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
		scores_.push( s_.score() );
		a.get( 0 ).doAction( s_ );
		action_history_.push( a );
	}

	@Override
	public void untakeLastAction()
	{
		final JointAction<YahtzeeAction> a = action_history_.pop();
		a.get( 0 ).undoAction( s_ );
		scores_.pop();
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
		return new double[] { s_.score() - scores_.peek() };
		
//		if( s_.isTerminal() ) {
//			return new double[] { s_.score() };
//		}
//		else {
//			return new double[] { 0 };
//		}
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
