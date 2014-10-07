/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * Extracts a single-agent policy from a joint policy.
 */
public class SingleAgentPolicyAdapter<S, A extends VirtualConstructor<A>>
	extends Policy<S, A>
{
	private final int i_;
	private final Policy<S, JointAction<A>> joint_;
	
	public SingleAgentPolicyAdapter( final int i, final Policy<S, JointAction<A>> joint )
	{
		i_ = i;
		joint_ = joint;
	}

	@Override
	public void setState( final S s, final long t )
	{ joint_.setState( s, t ); }

	@Override
	public A getAction()
	{
		final JointAction<A> j = joint_.getAction();
		return j.get( i_ );
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{
		return "SingleAgentPolicy[" + joint_.getName() + "/" + i_ + "]";
	}

	@Override
	public int hashCode()
	{
		return 7 + 11 * (joint_.hashCode() + 13 * i_);
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof SingleAgentPolicyAdapter<?, ?>) ) {
			return false;
		}
		
		@SuppressWarnings( "unchecked" )
		final SingleAgentPolicyAdapter<S, A> that = (SingleAgentPolicyAdapter<S, A>) obj;
		return joint_.equals( that.joint_ ) && i_ == that.i_;
	}
}
