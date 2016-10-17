/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * Adapts a policy over options to return options that always terminate
 * after one step.
 */
public class SingleStepAdapter<S, A> extends Policy<S, Option<S, A>>
{
	private final Policy<S, Option<S, A>> pi_;
	
	private final String str_;
	
	public <P extends Policy<S, Option<S, A>>> SingleStepAdapter( final P pi )
	{
		pi_ = pi;
		str_ = "SingleStepAdapter[" + pi_.getName() + "]";
	}
	
	@Override
	public int hashCode()
	{
		return 53 * pi_.hashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof SingleStepAdapter<?, ?>) ) {
			return false;
		}
		@SuppressWarnings( "unchecked" )
		final SingleStepAdapter<S, A> that = (SingleStepAdapter<S, A>) obj;
		return pi_.equals( that.pi_ );
	}

	@Override
	public void setState( final S s, final long t )
	{
		pi_.setState( s, t );
	}

	@Override
	public Option<S, A> getAction()
	{
		final Option<S, A> o = pi_.getAction();
		return new DurativeAction<S, A>( o.pi(), 1 );
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		pi_.actionResult( sprime, r );
	}

	@Override
	public String getName()
	{
		return str_;
	}
	
	@Override
	public String toString()
	{
		return getName();
	}

}
