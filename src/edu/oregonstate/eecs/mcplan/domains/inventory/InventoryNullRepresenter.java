/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.inventory;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ArrayFactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class InventoryNullRepresenter implements FactoredRepresenter<InventoryState, FactoredRepresentation<InventoryState>>
{
	private final ArrayList<Attribute> attributes;
	
	public InventoryNullRepresenter( final InventoryProblem problem )
	{
		attributes = new ArrayList<Attribute>( 3*problem.Nproducts );
		for( int i = 0; i < problem.Nproducts; ++i ) {
			attributes.add( new Attribute( "i" + i ) );
		}
		for( int i = 0; i < problem.Nproducts; ++i ) {
			attributes.add( new Attribute( "o" + i ) );
		}
		for( int i = 0; i < problem.Nproducts; ++i ) {
			attributes.add( new Attribute( "d" + i ) );
		}
	}
	
	private InventoryNullRepresenter( final ArrayList<Attribute> attributes )
	{
		this.attributes = attributes;
	}
	
	@Override
	public FactoredRepresenter<InventoryState, FactoredRepresentation<InventoryState>> create()
	{
		return new InventoryNullRepresenter( attributes );
	}

	@Override
	public FactoredRepresentation<InventoryState> encode( final InventoryState s )
	{
		final float[] phi = new float[3*s.problem.Nproducts];
		int idx = 0;
		for( int i = 0; i < s.problem.Nproducts; ++i ) {
			phi[idx++] = s.inventory[i];
		}
		for( int i = 0; i < s.problem.Nproducts; ++i ) {
			phi[idx++] = s.orders[i];
		}
		for( int i = 0; i < s.problem.Nproducts; ++i ) {
			phi[idx++] = s.demand[i];
		}
		return new ArrayFactoredRepresentation<InventoryState>( phi );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}
}
