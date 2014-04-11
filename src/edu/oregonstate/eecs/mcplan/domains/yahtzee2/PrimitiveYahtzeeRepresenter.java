/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class PrimitiveYahtzeeRepresenter implements FactoredRepresenter<YahtzeeState, FactoredRepresentation<YahtzeeState>>
{
	private static final ArrayList<Attribute> attributes_ = new ArrayList<Attribute>();
	private static final int Nattributes_ = Hand.Nfaces + 1 + (2 * YahtzeeScores.values().length) + 2;
	static {
		for( int i = 0; i < Hand.Nfaces; ++i ) {
			attributes_.add( new Attribute( "n" + (i+1) ) );
		}
		attributes_.add( new Attribute( "rerolls" ) );
		for( final YahtzeeScores category : YahtzeeScores.values() ) {
			attributes_.add( new Attribute( "filled_" + category ) );
		}
		for( final YahtzeeScores category : YahtzeeScores.values() ) {
			attributes_.add( new Attribute( "score_" + category ) );
		}
		attributes_.add( new Attribute( "yahtzee_bonus" ) );
		attributes_.add( new Attribute( "upper_bonus" ) );
	}
	
	@Override
	public PrimitiveYahtzeeRepresenter create()
	{
		return new PrimitiveYahtzeeRepresenter();
	}

	@Override
	public PrimitiveYahtzeeState encode( final YahtzeeState s )
	{
		return new PrimitiveYahtzeeState( s );
	}
	
	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes_;
	}
	
	@Override
	public String toString()
	{
		return "PrimitiveYahtzeeRepresenter";
	}

}
