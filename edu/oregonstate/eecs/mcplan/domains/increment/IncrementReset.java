/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.increment;

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

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.UndoableAction#undoAction(java.lang.Object)
	 */
	@Override
	public void undoAction( final IncrementState s )
	{
		assert( done_ );
		assert( s.counters[counter] == 0 );
		s.counters[counter] = (limit - 1) * (player == 0 ? -1 : +1);
		done_ = false;
	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.Action#doAction(java.lang.Object)
	 */
	@Override
	public void doAction( final IncrementState s )
	{
		assert( !done_ );
		assert( s.counters[counter] * (player == 0 ? -1 : +1) >= limit );
		s.counters[counter] = 0;
		done_ = true;
	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.Action#isDone()
	 */
	@Override
	public boolean isDone()
	{
		return done_;
	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.Action#create()
	 */
	@Override
	public IncrementEvent create()
	{
		return new IncrementReset( player, counter, limit );
	}
}
