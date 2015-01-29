/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

import edu.oregonstate.eecs.mcplan.Representation;

/**
 * @author jhostetler
 *
 */
public class IndexRepresentation<S> extends Representation<S>
{
	public final int index;
	
	public IndexRepresentation( final int index )
	{
		this.index = index;
	}
	
	@Override
	public IndexRepresentation<S> copy()
	{
		return new IndexRepresentation<S>( index );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof IndexRepresentation<?>) ) {
			return false;
		}
		@SuppressWarnings( "unchecked" )
		final IndexRepresentation<S> that = (IndexRepresentation<S>) obj;
		
		// TODO: Debugging code
		if( index == that.index ) {
			assert( hashCode() == that.hashCode() );
		}
		
		return index == that.index;
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ index;
	}

	@Override
	public String toString()
	{
		return "IndexRepresentation(" + Integer.toHexString( index ) + ")";
	}
}
