/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import java.util.ArrayDeque;
import java.util.Deque;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class SequentialJointSimulator<S, A extends VirtualConstructor<A>>
	implements UndoSimulator<S, JointAction<A>>
{
	private final int nagents_;
	private final UndoSimulator<S, A> base_;
	
	private final Deque<double[]> rhist_ = new ArrayDeque<double[]>();
	
	public SequentialJointSimulator( final int nagents, final UndoSimulator<S, A> base )
	{
		nagents_ = nagents;
		base_ = base;
		rhist_.push( new double[nagents] );
	}
	
	protected void foldReward( final double[] r, final double[] ri )
	{
		assert( r.length == ri.length );
		for( int i = 0; i < r.length; ++i ) {
			r[i] += ri[i];
		}
	}
	
	@Override
	public S state()
	{
		return base_.state();
	}

	@Override
	public void takeAction( final JointAction<A> a )
	{
		final double[] r = new double[nagents_];
		for( final A ai : a ) {
			base_.takeAction( ai );
			foldReward( r, base_.reward() );
		}
		rhist_.push( r );
	}

	@Override
	public void untakeLastAction()
	{
		for( int i = 0; i < nagents_; ++i ) {
			base_.untakeLastAction();
		}
		rhist_.pop();
	}

	@Override
	public long depth()
	{
		// TODO: Should we keep track of our own depth?
		return base_.depth();
	}

	@Override
	public long t()
	{
		// TODO: Should we keep track of our own t?
		return base_.t();
	}

	@Override
	public int nagents()
	{
		// TODO: The original sense of 'nagents' was "how many different
		// agents are making moves (ie. "what is the range of 'getTurn()'?").
		// Under the JointAction model, though, it has come to be conflated
		// with "how long is the reward vector"?. How to resolve this?
		return nagents_;
	}

	@Override
	public int turn()
	{
		// TODO: See 'getNumAgents()'
		return 0;
	}

	@Override
	public double[] reward()
	{
		return rhist_.peek();
	}

	@Override
	public boolean isTerminalState( )
	{
		return base_.isTerminalState( );
	}

	@Override
	public long horizon()
	{
		// TODO: Do we divide by getNumAgents()?
		return base_.horizon();
	}

	@Override
	public String detailString()
	{
		return "SequentialJointSimulator";
	}

}
