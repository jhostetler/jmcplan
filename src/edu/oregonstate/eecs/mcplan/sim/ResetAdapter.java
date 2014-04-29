/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class ResetAdapter<S, A extends VirtualConstructor<A>>
	implements ResetSimulator<S, A>, UndoSimulator<S, A>
{
	private final UndoSimulator<S, A> sim_;
	private final long d_;
	
	public ResetAdapter( final UndoSimulator<S, A> sim )
	{
		sim_ = sim;
		d_ = sim_.depth();
	}
	
	@Override
	public S state()
	{ return sim_.state(); }

	@Override
	public void takeAction( final JointAction<A> a )
	{ sim_.takeAction( a ); }

	@Override
	public long depth()
	{ return sim_.depth(); }

	@Override
	public long t()
	{ return sim_.t(); }

	@Override
	public int nagents()
	{ return sim_.nagents(); }

	@Override
	public int[] turn()
	{ return sim_.turn(); }

	@Override
	public double[] reward()
	{ return sim_.reward(); }

	@Override
	public boolean isTerminalState()
	{ return sim_.isTerminalState(); }

	@Override
	public long horizon()
	{ return sim_.horizon(); }

	@Override
	public String detailString()
	{ return "ResetAdapter[" + sim_.detailString() + "]"; }

	@Override
	public void untakeLastAction()
	{ sim_.untakeLastAction(); }

	@Override
	public void reset()
	{
		while( sim_.depth() > d_ ) {
//			System.out.println( sim_.depth() );
			sim_.untakeLastAction();
		}
	}
}
