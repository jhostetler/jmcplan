package edu.oregonstate.eecs.mcplan;


/**
 * A Policy that executes a particular action and then follows a specified
 * Policy afterwards.
 *
 * @param <S>
 * @param <A>
 */
public final class ConsPolicy<S, A extends VirtualConstructor<A>> extends Policy<S, A>
{
	private final A a0;
	private final Policy<S, A> pi;
	private boolean s0 = true;
	
	public ConsPolicy( final A a0, final Policy<S, A> pi )
	{
		this.a0 = a0;
		this.pi = pi;
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		// FIXME: We are planning to remove the 't' parameter. This is a
		// *temporary* hack.
		if( t == 0 ) {
			s0 = true;
		}
		else {
			s0 = false;
			pi.setState( s, t );
		}
	}

	@Override
	public A getAction()
	{
		if( s0 ) {
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
}
