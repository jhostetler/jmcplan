/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Forwards calls to improvePolicy() to a base policy, but then does what the
 * override policy wants to do instead. Mainly useful for testing.
 * 
 * @author jhostetler
 */
public class OverridePolicy<S, A> extends AnytimePolicy<S, A>
{
	private final AnytimePolicy<S, A> base;
	private final Policy<S, A> override;
	
	public OverridePolicy( final AnytimePolicy<S, A> base, final Policy<S, A> override )
	{
		this.base = base;
		this.override = override;
	}
	
	@Override
	public boolean improvePolicy()
	{
		return base.improvePolicy();
	}

	@Override
	public void setState( final S s, final long t )
	{
		base.setState( s, t );
		override.setState( s, t );
	}

	@Override
	public A getAction()
	{
		return override.getAction();
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{
		return "OverridePolicy";
	}

	@Override
	public int hashCode()
	{
		final HashCodeBuilder hb = new HashCodeBuilder();
		hb.append( getClass() ).append( base.hashCode() ).append( override.hashCode() );
		return hb.toHashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof OverridePolicy) ) {
			return false;
		}
		final OverridePolicy<S, A> that = (OverridePolicy<S, A>) obj;
		return base.equals( that.base ) && override.equals( that.override );
	}

}
