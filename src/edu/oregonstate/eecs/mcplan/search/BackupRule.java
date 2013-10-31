/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

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
	BackupRule<S, A> MaxQ()
	{
		return new BackupRule<S, A>()
			{
				@Override
				public double[] apply( final StateNode<S, A> sn )
				{ return BackupRules.MaxQ( sn ); }
			};
	}
	
	public static final <S, A extends VirtualConstructor<A>>
	BackupRule<S, A> MaxMinQ()
	{
		return new BackupRule<S, A>()
			{
				@Override
				public double[] apply( final StateNode<S, A> sn )
				{ return BackupRules.MaxMinQ( sn ); }
			};
	}
}
