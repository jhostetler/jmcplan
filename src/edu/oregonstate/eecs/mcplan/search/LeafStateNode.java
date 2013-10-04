/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class LeafStateNode<S, A extends VirtualConstructor<A>> extends StateNode<S, A>
{
	private final double[] v_;
	
	public LeafStateNode( final double[] v, final S token, final int nagents, final int turn )
	{
		super( token, nagents, turn );
		v_ = v;
	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.search.StateNode#v()
	 */
	@Override
	public double[] v()
	{
		return v_;
	}

}
