/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic.policy;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicAction;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicNothingAction;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicState;

/**
 * @author jhostetler
 *
 */
public class NothingPolicy extends AnytimePolicy<CosmicState, CosmicAction>
{
	@Override
	public void setState( final CosmicState s, final long t )
	{ }

	@Override
	public CosmicAction getAction()
	{
		return new CosmicNothingAction();
	}

	@Override
	public void actionResult( final CosmicState sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{
		return "NothingPolicy";
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode();
	}

	@Override
	public boolean equals( final Object that )
	{
		return that instanceof NothingPolicy;
	}
	
	@Override
	public String toString()
	{
		return "NothingPolicy";
	}

	@Override
	public boolean improvePolicy()
	{
		return false;
	}
}
