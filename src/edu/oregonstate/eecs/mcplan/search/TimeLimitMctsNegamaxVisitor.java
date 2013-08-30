/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.util.Countdown;

/**
 * @author jhostetler
 *
 */
public class TimeLimitMctsNegamaxVisitor<S, A> implements MctsNegamaxVisitor<S, A>
{
	private static final Logger log = LoggerFactory.getLogger( TimeLimitMctsNegamaxVisitor.class );
	
	private final MctsNegamaxVisitor<S, A> inner_;
	private final Countdown countdown_;
	private long start_time_ = 0L;
	
	public TimeLimitMctsNegamaxVisitor( final MctsNegamaxVisitor<S, A> inner, final Countdown countdown )
	{
		inner_ = inner;
		countdown_ = countdown;
	}
	
	@Override
	public void startEpisode( final S s )
	{
		inner_.startEpisode( s );
		start_time_ = System.currentTimeMillis();
	}

	@Override
	public boolean startRollout( final S s )
	{
		final boolean inner_result = inner_.startRollout( s );
		final long now = System.currentTimeMillis();
		countdown_.count( now - start_time_ );
		start_time_ = now;
		if( countdown_.expired() ) {
			log.debug( "*** Time limit" );
			return false;
		}
		else {
			return inner_result;
		}
	}

	@Override
	public void startTree( final S s )
	{
		inner_.startTree( s );
	}

	@Override
	public void treeAction( final A a, final S sprime )
	{
		inner_.treeAction( a, sprime );
	}

	@Override
	public void treeDepthLimit( final S s )
	{
		inner_.treeDepthLimit( s );
	}

	@Override
	public void startDefault( final S s )
	{
		inner_.startDefault( s );
	}

	@Override
	public void defaultAction( final A a, final S sprime )
	{
		inner_.defaultAction( a, sprime );
	}

	@Override
	public void defaultDepthLimit( final S s )
	{
		inner_.defaultDepthLimit( s );
	}

	@Override
	public void depthLimit( final S s )
	{
		inner_.depthLimit( s );
	}

	@Override
	public double terminal( final S s )
	{
		return inner_.terminal( s );
	}

	@Override
	public boolean isTerminal( final S s )
	{
		return inner_.isTerminal( s );
	}
}
