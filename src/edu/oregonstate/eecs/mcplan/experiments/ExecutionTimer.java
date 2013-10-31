/**
 * 
 */
package edu.oregonstate.eecs.mcplan.experiments;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.EpisodeListener;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class ExecutionTimer<S, A extends VirtualConstructor<A>> implements EpisodeListener<S, A>
{
	private final MeanVarianceAccumulator mv_ = new MeanVarianceAccumulator();
	private final MedianAccumulator median_ = new MedianAccumulator();
	private long start_time_ = 0L;
	private final boolean disabled_ = false;

	public MeanVarianceAccumulator meanVariance()
	{
		return mv_;
	}
	
	public MedianAccumulator median()
	{
		return median_;
	}

	@Override
	public <P extends Policy<S, JointAction<A>>> void startState( final S s, final P pi )
	{ }
	
	@Override
	public void preGetAction()
	{
		start_time_ = System.currentTimeMillis();
		System.out.println( "*** start_time = " + start_time_ );
	}

	@Override
	public void postGetAction( final JointAction<A> action )
	{
		final long tdiff = System.currentTimeMillis() - start_time_;
		System.out.println( "*** Elapsed time = " + tdiff );
		mv_.add( tdiff );
		median_.add( tdiff );
	}

	@Override
	public void onActionsTaken( final S sprime )
	{ }

	@Override
	public void endState( final S s )
	{ }
}
