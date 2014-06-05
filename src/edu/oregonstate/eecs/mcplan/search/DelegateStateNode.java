/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class DelegateStateNode<S, X extends Representation<S>, A extends VirtualConstructor<A>>
	extends MutableStateNode<S, X, A>
{
	private final BackupRule<X, A> backup_;
	private final double[] default_value_;
	
	// FIXME: Is the default_value mechanism a good way of handling this?
	public DelegateStateNode( final BackupRule<X, A> backup, final double[] default_value,
							  final X token, final int nagents, final int[] turn,
							  final ActionGenerator<S, JointAction<A>> action_gen )
	{
		super( token, nagents, turn, action_gen );
		backup_ = backup;
		default_value_ = default_value;
	}

//	@Override
	public double[] v()
	{
		// FIXME: This is a hack to avoid NP exception when backing up
		// un-expanded state nodes.
		if( successors().hasNext() ) {
			return backup_.apply( this );
		}
		else {
//			return Arrays.copyOf( default_value_, default_value_.length );
			return Arrays.copyOf( vhat_, nagents );
		}
	}
}
