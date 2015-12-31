/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * @author jhostetler
 *
 */
public final class StringRepresentation<S> extends Representation<S>
{
	private final String s;
	
	public StringRepresentation( final String s )
	{
		assert( s != null );
		this.s = s;
	}
	
	@Override
	public Representation<S> copy()
	{
		return new StringRepresentation<S>( s );
	}

	@Override
	public boolean equals( final Object obj )
	{
		@SuppressWarnings( "unchecked" )
		final StringRepresentation<S> that = (StringRepresentation<S>) obj;
		return s.equals( that.s );
	}

	@Override
	public int hashCode()
	{
		return s.hashCode();
	}
	
	@Override
	public String toString()
	{
		return s;
	}
}
