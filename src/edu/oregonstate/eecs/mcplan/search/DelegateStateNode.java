/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class DelegateStateNode<S, A extends VirtualConstructor<A>> extends StateNode<S, A>
{
	private final BackupRule<S, A> backup_;
	
	public DelegateStateNode( final BackupRule<S, A> backup, final S token, final int nagents, final int turn )
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
