/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import com.mathworks.toolbox.javabuilder.MWNumericArray;

/**
 * @author jhostetler
 *
 */
public class CosmicNothingAction extends CosmicAction
{
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.VirtualConstructor#create()
	 */
	@Override
	public CosmicAction create()
	{
		return new CosmicNothingAction();
	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicAction#toMatlab(edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicParameters, double)
	 */
	@Override
	public MWNumericArray toMatlab( final CosmicParameters params, final double t )
	{
		return new MWNumericArray();
	}
	
	@Override
	public void applyNonCosmicChanges( final CosmicState sprime )
	{ }

	@Override
	public int hashCode()
	{
		return getClass().hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof CosmicNothingAction;
	}

	@Override
	public String toString()
	{
		return "Nothing";
	}
	
}
