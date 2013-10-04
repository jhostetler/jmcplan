/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class MaxMinStateNode<S, A extends VirtualConstructor<A>> extends StateNode<S, JointAction<A>>
{

	public MaxMinStateNode( final S token, final int nagents, final int turn )
	{
		super( token, nagents, turn );
	}

	@Override
	public double[] v()
	{
		return BackupRules.MaxMinQ( this );
	}

}
