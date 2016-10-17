/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import org.apache.commons.math3.random.RandomGenerator;


/**
 * @author jhostetler
 *
 */
public class PassAction extends BlackjackAction
{
	public final int player;
	
	private boolean done_ = false;
	
	public PassAction( final int player )
	{
		this.player = player;
	}
	
	@Override
	public void undoAction( final BlackjackState s )
	{
		assert( done_ );
		s.setPassed( player, false );
		done_ = false;
	}

	@Override
	public void doAction( final RandomGenerator rng, final BlackjackState s )
	{
		assert( !done_ );
		s.setPassed( player, true );
		done_ = true;
	}

	@Override
	public boolean isDone()
	{
		return done_;
	}

	@Override
	public BlackjackAction create()
	{
		return new PassAction( player );
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof PassAction) ) {
			return false;
		}
		final PassAction that = (PassAction) obj;
		return player == that.player;
	}
	
	@Override
	public int hashCode()
	{
		return 37 + player;
	}
	
	@Override
	public String toString()
	{
		return "PassAction[" + player + "]";
	}
}
