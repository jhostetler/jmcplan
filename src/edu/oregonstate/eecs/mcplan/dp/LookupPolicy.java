/**
 * 
 */
package edu.oregonstate.eecs.mcplan.dp;

import java.util.Map;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class LookupPolicy<S, A extends VirtualConstructor<A>> extends Policy<S, A>
{
	private final Map<S, A> actions_;
	
	private A a_ = null;
	
	public LookupPolicy( final Map<S, A> actions )
	{
		actions_ = actions;
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		a_ = actions_.get( s );
	}

	@Override
	public A getAction()
	{
		return a_.create();
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{
		return "LookupPolicy";
	}

	@Override
	public int hashCode()
	{
		return actions_.hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof LookupPolicy) ) {
			return false;
		}
		final LookupPolicy<?, ?> that = (LookupPolicy<?, ?>) obj;
		return actions_.equals( that.actions_ );
	}
}
