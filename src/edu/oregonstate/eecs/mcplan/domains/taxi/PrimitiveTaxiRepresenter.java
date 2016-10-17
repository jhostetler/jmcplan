/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.taxi;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class PrimitiveTaxiRepresenter implements FactoredRepresenter<TaxiState, PrimitiveTaxiRepresentation>
{
	private final ArrayList<Attribute> attributes_;

	public PrimitiveTaxiRepresenter( final TaxiState s )
	{
		this( s.Nother_taxis, s.locations.size() );
	}
	
	public PrimitiveTaxiRepresenter( final int Nother_taxis, final int Nlocations )
	{
		attributes_ = new ArrayList<Attribute>();
		attributes_.add( new Attribute( "taxi_x" ) );
		attributes_.add( new Attribute( "taxi_y" ) );
		for( int i = 0; i < Nother_taxis; ++i ) {
			attributes_.add( new Attribute( "other" + i + "_x" ) );
			attributes_.add( new Attribute( "other" + i + "_y" ) );
		}
		attributes_.add( new Attribute( "passenger_taxi" ) );
		for( int i = 0; i < Nlocations; ++i ) {
			attributes_.add( new Attribute( "passenger_" + i ) );
		}
		for( int i = 0; i < Nlocations; ++i ) {
			attributes_.add( new Attribute( "dest_" + i ) );
		}
	}
	
	private PrimitiveTaxiRepresenter( final PrimitiveTaxiRepresenter that )
	{
		this.attributes_ = that.attributes_;
	}
	
	@Override
	public FactoredRepresenter<TaxiState, PrimitiveTaxiRepresentation> create()
	{
		return new PrimitiveTaxiRepresenter( this );
	}

	@Override
	public PrimitiveTaxiRepresentation encode( final TaxiState s )
	{
		return new PrimitiveTaxiRepresentation( s );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes_;
	}

}
