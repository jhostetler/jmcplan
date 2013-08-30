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
public class TimeLimitMctsVisitor<S, A> implements MctsVisitor<S, A>
{
	public static <S, A>
	TimeLimitMctsVisitor<S, A> create( final MctsVisitor<S, A> inner, final Countdown countdown )
	{
		return new TimeLimitMctsVisitor<S, A>( inner, countdown );
	}
	
	private static final Logger log = LoggerFactory.getLogger( TimeLimitMctsVisitor.class );
	
	private final MctsVisitor<S, A> inner_;
	private final Countdown countdown_;
	private long start_time_ = 0L;
	
	public TimeLimitMctsVisitor( final MctsVisitor<S, A> inner, final Countdown countdown )
	{
		inner_ = inner;
		countdown_ = countdown;
	}
	
	@Override
	public void startEpisode( final S s, final int nagents, final int turn )
	{
		inner_.startEpisode( s, nagents, turn );
		start_time_ = System.currentTimeMillis();
	}

	@Override
	public boolean startRollout( final S s, final int turn )
	{
		final boolean inner_result = inner_.startRollout( s, turn );
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
	public void startTree( final S s, final int turn )
	{
		inner_.startTree( s, turn );
	}

	@Override
	public void treeAction( final A a, final S sprime, final int next_turn )
	{
		inner_.treeAction( a, sprime, next_turn );
	}

	@Override
	public void treeDepthLimit( final S s, final int turn )
	{
		inner_.treeDepthLimit( s, turn );
	}

	@Override
	public void startDefault( final S s, final int turn )
	{
		inner_.startDefault( s, turn );
	}

	@Override
	public void defaultAction( final A a, final S sprime, final int next_turn )
	{
		inner_.defaultAction( a, sprime, next_turn );
	}

	@Override
	public void defaultDepthLimit( final S s, final int turn )
	{
		inner_.defaultDepthLimit( s, turn );
	}

	@Override
	public void depthLimit( final S s, final int turn )
	{
		inner_.depthLimit( s, turn );
	}

	@Override
	public double[] terminal( final S s, final int turn )
	{
		return inner_.terminal( s, turn );
	}

	@Override
	public boolean isTerminal( final S s, final int turn )
	{
		return inner_.isTerminal( s, turn );
	}
}
