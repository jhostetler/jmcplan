package edu.oregonstate.eecs.mcplan;


/**
 * A Policy that executes a particular action and then follows a specified
 * Policy afterwards.
 *
 * @param <S>
 * @param <A>
 */
public final class ConsPolicy<S, A extends VirtualConstructor<A>> extends AnytimePolicy<S, A>
{
	private final A a0;
	private final Policy<S, A> pi;
	/**
	 * 0: reset and no state set; 1: reset and first state set, will return
	 * a0 for the action; 2: following pi.
	 */
	private int nonstationary_step = 0;
	
	public ConsPolicy( final A a0, final Policy<S, A> pi )
	{
		this.a0 = a0;
		this.pi = pi;
	}
	
	@Override
	public void reset()
	{
		nonstationary_step = 0;
		pi.reset();
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		if( nonstationary_step > 0 ) {
			pi.setState( s, t );
		}
		if( nonstationary_step < 2 ) {
			nonstationary_step += 1;
		}
	}

	@Override
	public A getAction()
	{
		if( nonstationary_step == 1 ) {
			return a0.create();
		}
		else {
			return pi.getAction();
		}
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{ return getClass().toString(); }

	@Override
	public int hashCode()
	{ return System.identityHashCode( this ); }

	@Override
	public boolean equals( final Object that )
	{ return this == that; }
	
	@Override
	public String toString()
	{
		return "ConsPolicy(" + a0 + "; " + pi + ")";
	}

	@Override
	public boolean improvePolicy()
	{
		return false;
	}
}
