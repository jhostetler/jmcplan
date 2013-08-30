/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * Adapts a policy over options to return options that always terminate
 * after one step.
 */
public class SingleStepAdapter<S, A> implements Policy<S, Option<S, A>>
{
	private final Policy<S, Option<S, A>> pi_;
	
	private final String str_;
	
	public SingleStepAdapter( final Policy<S, Option<S, A>> pi )
	{
		pi_ = pi;
		str_ = "SingleStepAdapter[" + pi_.getName() + "]";
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
		return new DurativeAction<S, A>( o.pi, 1 );
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
