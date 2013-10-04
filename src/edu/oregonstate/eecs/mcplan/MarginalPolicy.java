/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * @author jhostetler
 *
 */
public class MarginalPolicy<S, A extends VirtualConstructor<A>> extends Policy<S, A>
{
	private final Policy<S, JointAction<A>> joint_policy_;
	private final int turn_;
	
	public <P extends Policy<S, JointAction<A>>> MarginalPolicy( final P joint_policy, final int turn )
	{
		joint_policy_ = joint_policy;
		turn_ = turn;
	}
	
	@Override
	public int hashCode()
	{
		return joint_policy_.hashCode() * 51 + turn_;
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof MarginalPolicy<?, ?>) ) {
			return false;
		}
		final MarginalPolicy<S, A> that = (MarginalPolicy<S, A>) obj;
		return turn_ == that.turn_ && joint_policy_.equals( that.joint_policy_ );
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
