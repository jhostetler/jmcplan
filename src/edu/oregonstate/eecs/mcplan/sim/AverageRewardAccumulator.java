/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class AverageRewardAccumulator<S, A extends VirtualConstructor<A>> implements EpisodeListener<S, A>
{
	public final MeanVarianceAccumulator[] reward;
	
	public AverageRewardAccumulator( final int Nagents )
	{
		reward = new MeanVarianceAccumulator[Nagents];
		for( int i = 0; i < Nagents; ++i ) {
			reward[i] = new MeanVarianceAccumulator();
		}
	}
	
	@Override
	public <P extends Policy<S, JointAction<A>>> void startState( final S s,
			final double[] r, final P pi )
	{ }

	@Override
	public void preGetAction()
	{ }

	@Override
	public void postGetAction( final JointAction<A> a )
	{ }

	@Override
	public void onActionsTaken( final S sprime, final double[] r )
	{
		for( int i = 0; i < r.length; ++i ) {
			reward[i].add( r[i] );
		}
	}

	@Override
	public void endState( final S s )
	{ }
}
