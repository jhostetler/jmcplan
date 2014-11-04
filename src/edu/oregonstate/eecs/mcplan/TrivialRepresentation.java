/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * The trivial representation, which maps all ground states to the same
 * abstract state.
 * 
 * @author jhostetler
 */
public class TrivialRepresentation<S> extends Representation<S>
{

	@Override
	public Representation<S> copy()
	{
		return new TrivialRepresentation<S>();
	}

	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof TrivialRepresentation<?>;
	}

	@Override
	public int hashCode()
	{
		return 3;
	}

}