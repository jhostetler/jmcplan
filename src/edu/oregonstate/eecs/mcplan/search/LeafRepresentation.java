/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;

/**
 * @author jhostetler
 *
 */
public class LeafRepresentation<S> extends FactoredRepresentation<S>
{
	@Override
	public LeafRepresentation<S> copy()
	{
		return new LeafRepresentation<S>();
	}

	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof LeafRepresentation<?>;
	}

	@Override
	public int hashCode()
	{
		return 3;
	}

	@Override
	public double[] phi()
	{
		return new double[] { };
	}
}
