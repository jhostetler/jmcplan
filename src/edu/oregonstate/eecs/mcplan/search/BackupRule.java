/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public abstract class BackupRule<S, A extends VirtualConstructor<A>>
	implements Fn.Function1<double[], StateNode<S, A>>
{
	public static final <S, A extends VirtualConstructor<A>>
	BackupRule<S, JointAction<A>> MaxMinQ()
	{
		return new BackupRule<S, JointAction<A>>()
			{
				@Override
				public double[] apply( final StateNode<S, JointAction<A>> sn )
				{ return BackupRules.MaxMinQ( sn ); }
			};
	}
}
