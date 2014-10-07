/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class SimpleMutableStateNode<S extends State, A extends VirtualConstructor<A>>
	extends MutableStateNode<S, A>
{
	public final Representation<S> x;
	
	public SimpleMutableStateNode( final Representation<S> x, final int nagents, final int[] turn,
							 final ActionGenerator<S, JointAction<A>> action_gen )
	{
		super( /*x,*/ nagents, turn, action_gen );
		this.x = x;
	}
	
	
	
	@Override
	public MutableActionNode<S, A> successor( final JointAction<A> a, final int nagents,
											  final Representer<S, ? extends Representation<S>> repr )
	{
		final MutableActionNode<S, A> an = getActionNode( a );
		if( an != null ) {
			return an;
		}
		else {
			final MutableActionNode<S, A> succ = createSuccessor( a, nagents, repr.create() );
			attachSuccessor( a, succ );
			return succ;
		}
	}
	
	protected MutableActionNode<S, A> createSuccessor(
			final JointAction<A> a, final int nagents, final Representer<S, ? extends Representation<S>> repr )
	{
		return new SimpleMutableActionNode<S, A>( a, nagents, repr );
	}
	
	@Override
	public String toString()
	{
		return "StateNode[" + x + "]";
	}

}