/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.oregonstate.eecs.mcplan.Representation;

/**
 * @author jhostetler
 *
 */
public class BlackjackStateToken extends Representation<BlackjackState>
{
	private final int hash_code_;
	private final String repr_;

	public BlackjackStateToken( final BlackjackState s )
	{
		final HashCodeBuilder h = new HashCodeBuilder();
		final StringBuilder sb = new StringBuilder();
		h.append( s.dealerHand() );
		sb.append( "d:" ).append( s.dealerHand().toString() );
		for( int i = 0; i < s.nplayers(); ++i ) {
			sb.append( ", " ).append( i ).append( ":" );
			h.append( s.hand( i ) );
			sb.append( s.hand( i ).toString() );
			h.append( s.passed( i ) );
			if( s.passed( i ) ) {
				sb.append( " passed" );
			}
		}
		hash_code_ = h.toHashCode();
		repr_ = sb.toString();
	}
	
	private BlackjackStateToken( final BlackjackStateToken that )
	{
		hash_code_ = that.hash_code_;
		repr_ = that.repr_;
	}

	@Override
	public int hashCode()
	{
		return hash_code_;
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof BlackjackStateToken) ) {
			return false;
		}
		final BlackjackStateToken that = (BlackjackStateToken) obj;
		return repr_.equals( that.repr_ );
	}
	
	@Override
	public String toString()
	{
		return repr_;
	}

	@Override
	public BlackjackStateToken copy()
	{
		return new BlackjackStateToken( this );
	}
}
