/**
 * 
 */
package edu.oregonstate.eecs.mcplan;


/**
 * @author jhostetler
 *
 */
public class RepeatPolicy<S, A extends VirtualConstructor<A>> extends AnytimePolicy<S, A>
{
	private final A a;
	
	public RepeatPolicy( final A a )
	{
		this.a = a;
	}

	@Override
	public void setState( final S s, final long t )
	{ }

	@Override
	public A getAction()
	{
		return a.create();
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{
		return "RepeatPolicy(" + a + ")";
	}
	
	@Override
	public String toString()
	{
		return getName();
	}

	@Override
	public boolean improvePolicy()
	{
		return false;
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ a.hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		@SuppressWarnings( "unchecked" )
		final RepeatPolicy<S, A> that = (RepeatPolicy<S, A>) obj;
		return a.equals( that.a );
	}

}
