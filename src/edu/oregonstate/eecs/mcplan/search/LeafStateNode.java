/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class LeafStateNode<S, A extends VirtualConstructor<A>>
	extends MutableStateNode<S, A>
{
	public LeafStateNode( /*final Representation<S> x,*/ final int nagents, final int[] turn )
	{
		// Note: Passing 'null' for action_gen.
		super( /*x,*/ nagents, turn, null );
	}

	@Override
	public MutableActionNode<S, A> successor( final JointAction<A> a, final int nagents,
			final Representer<S, ? extends Representation<S>> repr )
	{
		throw new UnsupportedOperationException( "LeafStateNode cannot have successors" );
	}

}
