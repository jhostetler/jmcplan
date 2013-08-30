/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * Adapts an AnytimePolicy into a Policy by always calling it with a fixed
 * 'control' value.
 */
public class FixedEffortPolicy<S, A> implements Policy<S, A>
{
	private final AnytimePolicy<S, A> anytime_;
	private final long control_;
	
	private final String str_;
	
	public FixedEffortPolicy( final AnytimePolicy<S, A> anytime, final long control )
	{
		anytime_ = anytime;
		control_ = control;
		str_ = "FixedEffortPolicy(" + control_ + ")[" + anytime_.getName() + "]";
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		anytime_.setState( s, t );
	}

	@Override
	public A getAction()
	{
		return anytime_.getAction( control_ );
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		anytime_.actionResult( sprime, r );
	}

	@Override
	public String getName()
	{
		return str_;
	}
}
