/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import java.util.ArrayList;

/**
 * @author jhostetler
 *
 */
public class HitAction extends BlackjackAction
{
	public final int player;
	
	private boolean done_ = false;
	private Card c_ = null;
	
	public HitAction( final int player )
	{
		this.player = player;
	}
	
	@Override
	public void undoAction( final BlackjackState s )
	{
		assert( done_ );
		s.deck().undeal( c_ );
		final ArrayList<Card> h = s.hand( player );
		h.remove( h.size() - 1 );
		s.setPassed( player, false ); // In case player busted
		done_ = false;
	}

	@Override
	public void doAction( final BlackjackState s )
	{
		assert( !done_ );
		c_ = s.deck().deal();
		s.hand( player ).add( c_ );
		if( s.parameters().handValue( s.hand( player ) )[0] > s.parameters().max_score ) {
			s.setPassed( player, true );
		}
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
		return new HitAction( player );
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof HitAction) ) {
			return false;
		}
		final HitAction that = (HitAction) obj;
		return player == that.player;
	}
	
	@Override
	public int hashCode()
	{
		return 41 + player;
	}
	
	@Override
	public String toString()
	{
		return "HitAction[" + player + "]";
	}
}
