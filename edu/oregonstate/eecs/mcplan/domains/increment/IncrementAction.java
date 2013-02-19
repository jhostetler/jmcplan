/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.increment;


/**
 * The only kind of action for the Increment Game.
 * 
 * @author jhostetler
 */
public class IncrementAction extends IncrementEvent
{
	public final int player;
	public final int counter;
	
	private boolean done_ = false;
	
	public IncrementAction( final int player, final int counter )
	{
		this.player = player;
		this.counter = counter;
	}

	@Override
	public void doAction( final IncrementState s )
	{
		assert( !done_ );
		s.counters[counter] += (player == 0 ? -1 : +1);
		done_ = true;
	}

	@Override
	public boolean isDone()
	{
		return done_;
	}

	@Override
	public IncrementAction create()
	{
		return new IncrementAction( player, counter );
	}

	@Override
	public void undoAction( final IncrementState s )
	{
		assert( done_ );
		s.counters[counter] -= (player == 0 ? -1 : +1);
		done_ = false;
	}
}
