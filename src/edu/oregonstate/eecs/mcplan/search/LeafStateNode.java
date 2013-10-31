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
public class LeafStateNode<S, X extends Representation<S>, A extends VirtualConstructor<A>>
	extends MutableStateNode<S, X, A>
{
	private final double[] v_;
	
	public LeafStateNode( final double[] v, final X token, final int nagents, final int[] turn )
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
