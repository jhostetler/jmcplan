/**
 * 
 */
package edu.oregonstate.eecs.mcplan.experiments;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveListener;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class ExecutionTimer<S, A extends UndoableAction<S>> implements SimultaneousMoveListener<S, A>
{
	private MeanVarianceAccumulator[] mv_ = null;
	private MedianAccumulator[] median_ = null;
	private long start_time_ = 0L;
	private final boolean disabled_ = false;
	
	public ExecutionTimer( final int Nplayers )
	{
		mv_ = new MeanVarianceAccumulator[Nplayers];
		for( int i = 0; i < Nplayers; ++i ) {
			mv_[i] = new MeanVarianceAccumulator();
		}
		
		median_ = new MedianAccumulator[Nplayers];
		for( int i = 0; i < Nplayers; ++i ) {
			median_[i] = new MedianAccumulator();
		}
	}
	
	public MeanVarianceAccumulator[] meanVariance()
	{
		return mv_;
	}
	
	public MedianAccumulator[] median()
	{
		return median_;
	}

	@Override
	public <P extends Policy<S, A>> void startState( final S s, final ArrayList<P> policies )
	{ }
	
	@Override
	public void preGetAction( final int player )
	{
		start_time_ = System.currentTimeMillis();
		System.out.println( "*** start_time = " + start_time_ );
	}

	@Override
	public void postGetAction( final int player, final UndoableAction<S> action )
	{
		final long tdiff = System.currentTimeMillis() - start_time_;
		System.out.println( "*** Elapsed time = " + tdiff );
		mv_[player].add( tdiff );
		median_[player].add( tdiff );
	}

	@Override
	public void onActionsTaken( final S sprime )
	{ }

	@Override
	public void endState( final S s )
	{ }
}
