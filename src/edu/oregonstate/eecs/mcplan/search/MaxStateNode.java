/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class MaxStateNode<S, X extends Representation<S>, A extends VirtualConstructor<A>>
	extends MutableStateNode<S, X, A>
{
	private final int player_;
	
	public MaxStateNode( final int player, final X token, final int nagents, final int turn )
	{
		super( token, nagents, turn );
		player_ = player;
	}

	@Override
	public double[] v()
	{
		return BackupRules.MarginalMaxQ( this, player_ );
	}

}
