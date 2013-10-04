/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class MaxStateNode<S, A extends VirtualConstructor<A>> extends StateNode<S, A>
{
	private final int player_;
	
	public MaxStateNode( final int player, final S token, final int nagents, final int turn )
	{
		super( token, nagents, turn );
		player_ = player;
	}

	@Override
	public double[] v()
	{
		return BackupRules.MaxQ( this, player_ );
	}

}
