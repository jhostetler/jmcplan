/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * @author jhostetler
 *
 */
public class IdentityRepresentation<S> extends Representation<S>
{
	private final String repr_;
	
	public IdentityRepresentation()
	{
		repr_ = this.getClass().toString();
	}
	
	public IdentityRepresentation( final String repr )
	{
		repr_ = repr;
	}

	@Override
	public Representation<S> copy()
	{
		return new IdentityRepresentation<S>();
	}

	@Override
	public boolean equals( final Object obj )
	{
		return this == obj;
	}

	@Override
	public int hashCode()
	{
		return System.identityHashCode( this );
	}
	
	@Override
	public String toString()
	{
		return repr_;
	}

}
