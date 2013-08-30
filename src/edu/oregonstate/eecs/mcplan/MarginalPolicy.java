/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * @author jhostetler
 *
 */
public class MarginalPolicy<S, A extends VirtualConstructor<A>> implements Policy<S, A>
{
	private final Policy<S, JointAction<A>> joint_policy_;
	private final int turn_;
	
	public MarginalPolicy( final Policy<S, JointAction<A>> joint_policy, final int turn )
	{
		joint_policy_ = joint_policy;
		turn_ = turn;
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		joint_policy_.setState( s, t );
	}

	@Override
	public A getAction()
	{
		return joint_policy_.getAction().get( turn_ );
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		joint_policy_.actionResult( sprime, r );
	}

	@Override
	public String getName()
	{
		return "MarginalPolicy";
	}
}
