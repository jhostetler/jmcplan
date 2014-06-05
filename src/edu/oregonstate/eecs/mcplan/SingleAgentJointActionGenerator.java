/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * @author jhostetler
 *
 */
public class SingleAgentJointActionGenerator<S, A extends VirtualConstructor<A>>
	extends ActionGenerator<S, JointAction<A>>
{
	public static <S, A extends VirtualConstructor<A>>
	SingleAgentJointActionGenerator<S, A> create( final ActionGenerator<S, A> base )
	{
		return new SingleAgentJointActionGenerator<S, A>( base );
	}
	
	private final ActionGenerator<S, A> base_;
	
	public SingleAgentJointActionGenerator( final ActionGenerator<S, A> base )
	{
		base_ = base;
	}
	
	@Override
	public ActionGenerator<S, JointAction<A>> create()
	{
		return new SingleAgentJointActionGenerator<S, A>( base_.create() );
	}

	@Override
	public void setState( final S s, final long t, final int[] turn )
	{
		base_.setState( s, t, turn );
	}

	@Override
	public int size()
	{
		return base_.size();
	}

	@Override
	public boolean hasNext()
	{
		return base_.hasNext();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public JointAction<A> next()
	{
		return new JointAction<A>( base_.next() );
	}
}
