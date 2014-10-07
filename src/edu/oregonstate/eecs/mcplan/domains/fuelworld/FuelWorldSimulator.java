/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.fuelworld;

import java.util.ArrayDeque;
import java.util.Deque;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;

/**
 * @author jhostetler
 *
 */
public class FuelWorldSimulator implements UndoSimulator<FuelWorldState, FuelWorldAction>
{
	private final FuelWorldState s_;
	
	private final Deque<FuelWorldAction> action_history_ = new ArrayDeque<FuelWorldAction>();
	
	public FuelWorldSimulator( final FuelWorldState s )
	{
		s_ = s;
	}
	
	@Override
	public FuelWorldState state()
	{
		return s_;
	}

	@Override
	public void takeAction( final JointAction<FuelWorldAction> a )
	{
		final FuelWorldAction a0 = a.get( 0 );
		a0.doAction( s_ );
		action_history_.push( a0 );
		
		s_.t += 1;
		assert( s_.t <= s_.T );
	}
	
	@Override
	public void untakeLastAction()
	{
		s_.t -= 1;
		assert( s_.t >= 0 );
		
		final FuelWorldAction a = action_history_.pop();
		a.undoAction( s_ );
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
		if( s_.location == s_.goal ) {
			return new double[] { -1 + 20 }; //0 };
//			return new double[] { 0 };
		}
		else if( s_.out_of_fuel && s_.fuel == 0 ) {
			return new double[] { -1 + -100 }; //-20 - (s_.T - s_.t) };
//			return new double[] { -20 - (s_.T - s_.t) };
		}
		else {
			return new double[] { -1 };
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
		return s_.T - s_.t;
	}

	@Override
	public String detailString()
	{
		return "FuelWorldSimulator";
	}
}
