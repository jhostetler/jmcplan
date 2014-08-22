/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class BlackjackAggregator implements FactoredRepresenter<BlackjackState, FactoredRepresentation<BlackjackState>>
{
	private static ArrayList<Attribute> attributes_;
	
	static {
		attributes_ = new ArrayList<Attribute>();
		attributes_.add( new Attribute( "pv" ) );
		attributes_.add( new Attribute( "pa" ) );
		attributes_.add( new Attribute( "dv" ) );
	}
	
	
	@Override
	public BlackjackAggregator create()
	{
		return new BlackjackAggregator();
	}

	@Override
	public HandValueAbstraction encode( final BlackjackState s )
	{
		return new HandValueAbstraction( s );
	}

	@Override
	public String toString()
	{
		return "value";
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes_;
	}
}
