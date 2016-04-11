/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author jhostetler
 *
 */
public class SequencePolicy<S, A> extends Policy<S, A>
{
	private final List<Policy<S, A>> policies;
	private final int[] switch_times;
	
	private int t = 0;
	private int i = 0;
	
	public SequencePolicy( final List<Policy<S, A>> policies, final int[] switch_times )
	{
		assert( policies.size() - 1 == switch_times.length );
		this.policies = policies;
		this.switch_times = switch_times;
	}
	
	@Override
	public void reset()
	{
		t = 0;
		i = 0;
		policies.get( i ).reset();
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		if( i < switch_times.length && this.t == switch_times[i] ) {
			i += 1;
			policies.get( i ).reset();
		}
		policies.get( i ).setState( s, t );
		this.t += 1; // FIXME: Assuming discrete time steps
	}

	@Override
	public A getAction()
	{
		return policies.get( i ).getAction();
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		policies.get( i ).actionResult( sprime, r );
	}

	@Override
	public String getName()
	{
		return "SequencePolicy";
	}

	@Override
	public int hashCode()
	{
		final HashCodeBuilder hb = new HashCodeBuilder();
		hb.append( getClass() ).append( policies ).append( Arrays.hashCode( switch_times ) );
		return hb.toHashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof SequencePolicy<?, ?>) ) {
			return false;
		}
		@SuppressWarnings( "unchecked" )
		final SequencePolicy<S, A> that = (SequencePolicy<S, A>) obj;
		return policies.equals( that.policies ) && Arrays.equals( switch_times, that.switch_times );
	}

}
