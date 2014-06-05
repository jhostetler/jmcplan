/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tamarisk;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class IndicatorTamariskRepresenter implements FactoredRepresenter<TamariskState, FactoredRepresentation<TamariskState>>
{
	private final ArrayList<Attribute> attributes_;

	public IndicatorTamariskRepresenter( final TamariskParameters p )
	{
		attributes_ = new ArrayList<Attribute>();
		for( int r = 0; r < p.Nreaches; ++r ) {
			for( int h = 0; h < p.Nhabitats; ++h ) {
				attributes_.add( new Attribute( "r" + r + "h" + h + "_Native" ) );
				attributes_.add( new Attribute( "r" + r + "h" + h + "_Tamarisk" ) );
			}
		}
	}
	
	private IndicatorTamariskRepresenter( final IndicatorTamariskRepresenter that )
	{
		attributes_ = that.attributes_;
	}
	
	@Override
	public IndicatorTamariskRepresenter create()
	{
		return new IndicatorTamariskRepresenter( this );
	}

	@Override
	public IndicatorTamariskRepresentation encode( final TamariskState s )
	{
		return new IndicatorTamariskRepresentation( s );
	}
	
	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes_;
	}
	
	@Override
	public String toString()
	{
		return "IndicatorTamariskRepresenter";
	}
}