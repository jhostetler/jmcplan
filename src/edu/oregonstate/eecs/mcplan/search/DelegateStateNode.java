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
public class DelegateStateNode<S, X extends Representation<S>, A extends VirtualConstructor<A>>
	extends MutableStateNode<S, X, A>
{
	private final BackupRule<X, A> backup_;
	
	public DelegateStateNode( final BackupRule<X, A> backup, final X token, final int nagents, final int[] turn )
	{
		super( token, nagents, turn );
		backup_ = backup;
	}

	@Override
	public double[] v()
	{
		return backup_.apply( this );
	}
}
