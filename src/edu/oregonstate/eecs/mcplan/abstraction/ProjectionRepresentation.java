/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;

/**
 * @author jhostetler
 *
 */
public class ProjectionRepresentation<S> extends FactoredRepresentation<S>
{
	public final int[] indices;
	private final double[] proj;
	
	public ProjectionRepresentation( final int[] indices, final double[] phi )
	{
		this.indices = indices;
		proj = new double[indices.length];
		for( int i = 0; i < indices.length; ++i ) {
			proj[i] = phi[indices[i]];
		}
	}
	
	private ProjectionRepresentation( final double[] proj, final int[] indices )
	{
		this.proj = proj;
		this.indices = indices;
	}
	
	@Override
	public double[] phi()
	{
		return proj;
	}

	@Override
	public ProjectionRepresentation<S> copy()
	{
		return new ProjectionRepresentation<S>( proj, indices );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( this == obj ) {
			return true;
		}
		if( obj == null ) {
			return false;
		}
		if( getClass() != obj.getClass() ) {
			return false;
		}
		final ProjectionRepresentation<?> other = (ProjectionRepresentation<?>) obj;
		if( !Arrays.equals( indices, other.indices ) ) {
			throw new UnsupportedOperationException( "Comparing Projections onto different index sets" );
		}
		if( !Arrays.equals( proj, other.proj ) ) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode( indices );
		result = prime * result + Arrays.hashCode( proj );
		return result;
	}

}
