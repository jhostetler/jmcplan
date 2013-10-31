/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager.policies;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;

/**
 * @author jhostetler
 *
 */
public class DefensivePolicy extends Policy<VoyagerState, VoyagerAction>
{
	
	@Override
	public int hashCode()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean equals( final Object that )
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void setState( final VoyagerState s, final long t )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public VoyagerAction getAction()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void actionResult( final VoyagerState sprime, final double[] r )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
