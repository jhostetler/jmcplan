package edu.oregonstate.eecs.mcplan.domains.blackjack;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.domains.cards.Card;

public class BlackjackPrimitiveRepresenter implements FactoredRepresenter<BlackjackState, FactoredRepresentation<BlackjackState>>
{
	private final int nagents_;
	private final ArrayList<Attribute> attributes_;
	
	public BlackjackPrimitiveRepresenter( final int nagents )
	{
		nagents_ = nagents;
		attributes_ = new ArrayList<Attribute>();
		for( int p = 0; p < nagents_ + 1; ++p ) {
			final String player = (p < nagents_ ? "p" + p : "d");
			for( int i = 0; i < 52; ++i ) {
				final Card c = Card.values()[i];
				attributes_.add( new Attribute( player + "_" + c ) );
			}
		}
		assert( attributes_.size() == (nagents_ + 1)*52 );
	}
	
	private BlackjackPrimitiveRepresenter( final BlackjackPrimitiveRepresenter that )
	{
		nagents_ = that.nagents_;
		attributes_ = that.attributes_;
	}
	
	@Override
	public BlackjackPrimitiveRepresenter create()
	{
		return new BlackjackPrimitiveRepresenter( this );
	}

	@Override
	public BlackjackStateToken encode( final BlackjackState s )
	{
		return new BlackjackStateToken( s );
	}
	
	@Override
	public String toString()
	{
		return "flat";
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes_;
	}
}