/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A non-stationary Policy that cycles through a list of Policies depending on
 * the absolute time.
 */
public class PhasedPolicy<S, A> extends Policy<S, A>
{
	public static <S, A> PhasedPolicy<S, A> create( final ArrayList<Policy<S, A>> Pi, final int[] ts )
	{
		return new PhasedPolicy<S, A>( Pi, ts );
	}
	
	private final ArrayList<Policy<S, A>> Pi_;
	private final int[] ts_;
	
	private Policy<S, A> pi_ = null;
	
	/**
	 * @param Pi
	 * @param ts The *start* times for the policies in Pi
	 */
	public PhasedPolicy( final ArrayList<Policy<S, A>> Pi, final int[] ts )
	{
		Pi_ = Pi;
		ts_ = ts;
	}
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder( 311, 313 ).append( Pi_ ).append( ts_ ).toHashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof PhasedPolicy<?, ?>) ) {
			return false;
		}
		final PhasedPolicy<?, ?> that = (PhasedPolicy<?, ?>) obj;
		return Pi_.equals( that.Pi_ ) && Arrays.equals( ts_, that.ts_ );
	}

	@Override
	public void setState( final S s, final long t )
	{
		for( int i = 0; i < ts_.length; ++i ) {
			if( t < ts_[i] ) {
				pi_ = Pi_.get( i - 1 );
				pi_.setState( s, t );
				return;
			}
		}
		pi_ = Pi_.get( Pi_.size() - 1 );
		pi_.setState( s, t );
	}

	@Override
	public A getAction()
	{
		return pi_.getAction();
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		pi_.actionResult( sprime, r );
	}

	@Override
	public String getName()
	{
		return "PhasedPolicy";
	}

}
