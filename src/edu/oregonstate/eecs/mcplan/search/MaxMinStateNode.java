/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class MaxMinStateNode<S, X extends Representation<S>, A extends VirtualConstructor<A>>
	extends MutableStateNode<S, X, JointAction<A>>
{

	public MaxMinStateNode( final X token, final int nagents, final int turn )
	{
		super( token, nagents, turn );
	}

	@Override
	public double[] v()
	{
		return BackupRules.MaxMinQ( this );
	}

}
