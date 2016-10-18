/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.increment;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public class IncrementReset extends IncrementEvent
{
	public final int player;
	public final int counter;
	public final int limit;
	
	private boolean done_ = false;
	
	/**
	 * 
	 */
	public IncrementReset( final int player, final int counter, final int limit )
	{
		this.player = player;
		this.counter = counter;
		this.limit = limit;
	}
	
	@Override
	public void undoAction( final IncrementState s )
	{
		assert( done_ );
		assert( s.counters[counter] == 0 );
		s.counters[counter] = (limit - 1) * (player == 0 ? -1 : +1);
		done_ = false;
	}

	@Override
	public void doAction( final RandomGenerator rng, final IncrementState s )
	{
		assert( !done_ );
		assert( s.counters[counter] * (player == 0 ? -1 : +1) >= limit );
		s.counters[counter] = 0;
		done_ = true;
	}

	@Override
	public boolean isDone()
	{
		return done_;
	}

	@Override
	public IncrementEvent create()
	{
		return new IncrementReset( player, counter, limit );
	}
}
