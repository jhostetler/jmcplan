/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

import java.util.ArrayList;
import java.util.Arrays;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class ProjectionRepresenter<S> implements FactoredRepresenter<S, ProjectionRepresentation<S>>
{
	private final FactoredRepresenter<S, ? extends FactoredRepresentation<S>> base_repr;
	private final int[] indices;
	
	private final ArrayList<Attribute> attributes = new ArrayList<Attribute>();
	
	public ProjectionRepresenter(
		final FactoredRepresenter<S, ? extends FactoredRepresentation<S>> base_repr, final int[] indices )
	{
		this.base_repr = base_repr;
		this.indices = indices;
		
		Arrays.sort( indices );
		final ArrayList<Attribute> base_attributes = base_repr.attributes();
		for( final int i : indices ) {
			attributes.add( base_attributes.get( i ) );
		}
	}
	
	@Override
	public FactoredRepresenter<S, ProjectionRepresentation<S>> create()
	{
		return new ProjectionRepresenter<S>( base_repr.create(), indices );
	}

	@Override
	public ProjectionRepresentation<S> encode( final S s )
	{
		final double[] x = base_repr.encode( s ).phi();
		return new ProjectionRepresentation<S>( indices, x );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}
}
