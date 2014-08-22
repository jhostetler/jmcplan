/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.State;

/**
 * @author jhostetler
 *
 */
public class BlackjackState implements State
{
	private final Deck deck_;
	
	private final ArrayList<Card> dealer_hand_ = new ArrayList<Card>();
	
	private final int nplayers_;
	
	private final boolean[] passed_;
	
	private final ArrayList<ArrayList<Card>> hands_;
	
	private final BlackjackParameters params_;
	
	public BlackjackState( final Deck deck, final int nplayers, final BlackjackParameters params )
	{
		deck_ = deck;
		nplayers_ = nplayers;
		params_ = params;
		dealer_hand_.add( deck_.deal() ); // Upcard
		passed_ = new boolean[nplayers_];
		hands_ = new ArrayList<ArrayList<Card>>( nplayers_ );
		for( int i = 0; i < nplayers_; ++i ) {
			final ArrayList<Card> h = new ArrayList<Card>();
			h.add( deck_.deal() );
			h.add( deck_.deal() );
			hands_.add( h );
		}
	}
	
	public BlackjackParameters parameters()
	{
		return params_;
	}
	
	public Deck deck()
	{
		return deck_;
	}
	
	public int nplayers()
	{
		return nplayers_;
	}
	
	public ArrayList<Card> hand( final int i )
	{
		return hands_.get( i );
	}
	
	public boolean passed( final int i )
	{
		return passed_[i];
	}
	
	public void setPassed( final int i, final boolean passed )
	{
		passed_[i] = passed;
	}
	
	public Card dealerUpcard()
	{
		return dealer_hand_.get( 0 );
	}
	
	public ArrayList<Card> dealerHand()
	{
		return dealer_hand_;
	}

//	@Override
//	public BlackjackStateToken token()
//	{
//		return new BlackjackStateToken( this );
//	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "d: " ).append( dealer_hand_ );
		for( int i = 0; i < hands_.size(); ++i ) {
			sb.append( ", " ).append( i ).append( ": " ).append( hands_.get( i ) );
		}
		return sb.toString();
//		return "STRING NOT IMPLEMENTED!"; //token().toString();
	}

	@Override
	public boolean isTerminal()
	{
		for( final boolean b : passed_ ) {
			if( !b ) {
				return false;
			}
		}
		return true;
	}
}
