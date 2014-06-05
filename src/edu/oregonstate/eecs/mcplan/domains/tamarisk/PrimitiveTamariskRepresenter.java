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
public class PrimitiveTamariskRepresenter implements FactoredRepresenter<TamariskState, FactoredRepresentation<TamariskState>>
{
	private final ArrayList<Attribute> attributes_;

	public PrimitiveTamariskRepresenter( final TamariskParameters p )
	{
		attributes_ = new ArrayList<Attribute>();
		for( int r = 0; r < p.Nreaches; ++r ) {
			for( int h = 0; h < p.Nhabitats; ++h ) {
				attributes_.add( new Attribute( "r" + r + "h" + h ) );
			}
		}
	}
	
	private PrimitiveTamariskRepresenter( final PrimitiveTamariskRepresenter that )
	{
		attributes_ = that.attributes_;
	}
	
	@Override
	public PrimitiveTamariskRepresenter create()
	{
		return new PrimitiveTamariskRepresenter( this );
	}

	@Override
	public PrimitiveTamariskRepresentation encode( final TamariskState s )
	{
		return new PrimitiveTamariskRepresentation( s );
	}
	
	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes_;
	}
	
	@Override
	public String toString()
	{
		return "PrimitiveTamariskRepresenter";
	}
}
