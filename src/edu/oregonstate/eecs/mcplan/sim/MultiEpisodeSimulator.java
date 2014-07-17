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
public class MultiEpisodeSimulator<S, A extends VirtualConstructor<A>> implements Simulator<S, A>
{

	@Override
	public S state()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void takeAction( final JointAction<A> a )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public long depth()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long t()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int nagents()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] turn()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] reward()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isTerminalState()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long horizon()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String detailString()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
