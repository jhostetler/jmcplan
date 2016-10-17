/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.pats;

import edu.oregonstate.eecs.mcplan.sim.StateNode;

/**
 * @author jhostetler
 *
 */
public class SampleTree<S, A>
{
	private final StateNode<S, A> s0;
	private final int w;
	
	public SampleTree( final S s0, final int w )
	{
		this.s0 = s0;
		this.w = w;
	}
	
	
}
