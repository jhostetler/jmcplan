package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.util.Countdown;

/**
 * A ForwardingNegamaxVisitor that tracks total execution time and stops the
 * search when time exceeds a limit. Time is measured on each call to
 * discoverVertex(), so actual time used may be quite a bit longer than
 * requested.
 * 
 * @author jhostetler
 *
 * @param <S>
 * @param <A>
 */
public class BoundedVisitor<S, A> extends ForwardingNegamaxVisitor<S, A>
{
	public final Countdown countdown_;
	private long start_time_ = 0L;
	
	public PrincipalVariation<S, A> pv_ = null;
	
	public BoundedVisitor( final NegamaxVisitor<S, A> inner, final Countdown countdown )
	{
		super( inner );
		countdown_ = countdown;
	}
	
	@Override
	public void startVertex( final S s )
	{
		super.startVertex( s );
		start_time_ = System.currentTimeMillis();
	}
	
	@Override
	public boolean discoverVertex( final S s )
	{
		final boolean inner_result = super.discoverVertex( s );
		final long now = System.currentTimeMillis();
		countdown_.count( now - start_time_ );
		start_time_ = now;
		if( countdown_.expired() ) {
			System.out.println( "*** Time limit" );
			return true;
		}
		else {
			return inner_result;
	
		}
	}
	
	@Override
	public void principalVariation( final PrincipalVariation<S, A> pv )
	{
		pv_ = pv;
		super.principalVariation( pv );
	}
}