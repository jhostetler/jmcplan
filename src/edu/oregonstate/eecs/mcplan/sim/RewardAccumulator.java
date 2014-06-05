/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class RewardAccumulator<S, A extends VirtualConstructor<A>> implements EpisodeListener<S, A>
{
	public final double discount;
	private double running_discount_ = 1.0;
	private final double[] v_;
	private int steps_ = 0;
	
	public RewardAccumulator( final int nagents, final double discount )
	{
		this.discount = discount;
		v_ = new double[nagents];
	}
	
	public double[] v()
	{
		return Arrays.copyOf( v_, v_.length );
	}
	
	public int steps()
	{
		return steps_;
	}
	
	@Override
	public <P extends Policy<S, JointAction<A>>> void startState( final S s,
			final double[] r, final P pi )
	{
		Fn.vplus_inplace( v_, r );
		running_discount_ *= discount;
//		System.out.println( "start" );
	}

	@Override
	public void preGetAction()
	{ }

	@Override
	public void postGetAction( final JointAction<A> a )
	{ }

	@Override
	public void onActionsTaken( final S sprime, final double[] r )
	{
		Fn.vplus_ax_inplace( v_, running_discount_, r );
		running_discount_ *= discount;
		steps_ += 1;
//		System.out.println( "step" );
	}

	@Override
	public void endState( final S s )
	{ }
}
