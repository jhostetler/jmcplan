/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class MeanStateNode<S, A extends VirtualConstructor<A>> extends StateNode<S, A>
{

	public MeanStateNode( final S token, final int nagents, final int turn )
	{
		super( token, nagents, turn );
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.search.StateNode#v()
	 */
	@Override
	public double[] v()
	{
		return BackupRules.MeanQ( this );
	}

}
